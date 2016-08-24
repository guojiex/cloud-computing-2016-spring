import java.io.IOException;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

public class Coordinator extends Verticle {

    // This integer variable tells you what region you are in
    // 1 for US-E, 2 for US-W, 3 for Singapore
    private static int region = KeyValueLib.region;

    // Default mode: Strongly consistent
    // Options: strong, eventual
    private static String consistencyType = "strong";

    /**
     * TODO: Set the values of the following variables to the DNS names of your
     * three dataCenter instances. Be sure to match the regions with their DNS!
     * Do the same for the 3 Coordinators as well.
     */
    private static final String dataCenterUSE = "ec2-52-91-176-99.compute-1.amazonaws.com";
    private static final String dataCenterUSW = "ec2-54-209-206-148.compute-1.amazonaws.com";
    private static final String dataCenterSING = "ec2-52-23-224-33.compute-1.amazonaws.com";

    private static final String coordinatorUSE = "ec2-54-209-55-56.compute-1.amazonaws.com";
    private static final String coordinatorUSW = "ec2-54-209-199-154.compute-1.amazonaws.com";
    private static final String coordinatorSING = "ec2-52-87-244-108.compute-1.amazonaws.com";

    public static int myHash(String key) {
        int res = 0;
        for (int i = 0; i < key.length(); i++) {
            res += Math.abs(key.charAt(i) - 'a');
        }
        return res % 3;
    }

    // private static HashSet lock
    @Override
    public void start() {
        KeyValueLib.dataCenters.put(dataCenterUSE, 1);
        KeyValueLib.dataCenters.put(dataCenterUSW, 2);
        KeyValueLib.dataCenters.put(dataCenterSING, 3);
        KeyValueLib.coordinators.put(coordinatorUSE, 1);
        KeyValueLib.coordinators.put(coordinatorUSW, 2);
        KeyValueLib.coordinators.put(coordinatorSING, 3);
        final RouteMatcher routeMatcher = new RouteMatcher();
        final HttpServer server = vertx.createHttpServer();
        server.setAcceptBacklog(32767);
        server.setUsePooledBuffers(true);
        server.setReceiveBufferSize(4 * 1024);

        routeMatcher.get("/put", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                MultiMap map = req.params();
                final String key = map.get("key");
                final String value = map.get("value");
                final Long timestamp = Long.parseLong(map.get("timestamp"));
                final String forwarded = map.get("forward");
                final String forwardedRegion = map.get("region");

                Thread t = new Thread(new Runnable() {
                    public void eventualConsistency() {
                        int currentRegion = KeyValueLib.region;
                        int targetRegion = myHash(key) + 1;
                        if (currentRegion == targetRegion || forwarded != null) {
                            Thread[] temp = new Thread[KeyValueLib.dataCenters.keySet().size()];
                            int count = 0;
                            for (String dcDNS : KeyValueLib.dataCenters.keySet()) {
                                temp[count] = new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            KeyValueLib.PUT(dcDNS, key, value, String.valueOf(timestamp),
                                                    consistencyType);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                temp[count].start();
                                count++;
                            }
                            return;
                        } else {
                            try {
                                switch (targetRegion) {
                                case 1:
                                    KeyValueLib.FORWARD(coordinatorUSE, key, value, String.valueOf(timestamp));
                                    break;
                                case 2:
                                    KeyValueLib.FORWARD(coordinatorUSW, key, value, String.valueOf(timestamp));
                                    break;
                                case 3:
                                    KeyValueLib.FORWARD(coordinatorSING, key, value, String.valueOf(timestamp));
                                    break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    public void strongConsistency() {
                        int currentRegion = KeyValueLib.region;
                        int targetRegion = myHash(key) + 1;
                        if (currentRegion == targetRegion || forwarded != null) {
                            if (forwarded == null) {
                                try {
                                    // lock all dc
                                    KeyValueLib.AHEAD(key, String.valueOf(timestamp));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            Thread[] temp = new Thread[KeyValueLib.dataCenters.keySet().size()];
                            int count = 0;
                            for (String dcDNS : KeyValueLib.dataCenters.keySet()) {
                                temp[count] = new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            KeyValueLib.PUT(dcDNS, key, value, String.valueOf(timestamp),
                                                    consistencyType);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                temp[count].start();
                                count++;
                            }
                            for (Thread temp2 : temp) {
                                try {
                                    temp2.join();// sync
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {// unlock dc
                                KeyValueLib.COMPLETE(key, String.valueOf(timestamp));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return;
                        } else {
                            try {
                                try {
                                    // lock all dc
                                    KeyValueLib.AHEAD(key, String.valueOf(timestamp));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                switch (targetRegion) {
                                case 1:
                                    KeyValueLib.FORWARD(coordinatorUSE, key, value, String.valueOf(timestamp));
                                    break;
                                case 2:
                                    KeyValueLib.FORWARD(coordinatorUSW, key, value, String.valueOf(timestamp));
                                    break;
                                case 3:
                                    KeyValueLib.FORWARD(coordinatorSING, key, value, String.valueOf(timestamp));
                                    break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    public void run() {
                        if (consistencyType.equals("strong"))
                            this.strongConsistency();
                        else
                            this.eventualConsistency();
                    }
                });
                t.start();
                req.response().end(); // Do not remove this
                req.response().close();
            }
        });

        routeMatcher.get("/get", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                MultiMap map = req.params();
                final String key = map.get("key");
                final Long timestamp = Long.parseLong(map.get("timestamp"));
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        int currentRegion = KeyValueLib.region;
                        String response = null;
                        try {
                            switch (currentRegion) {
                            case 1:
                                response = KeyValueLib.GET(dataCenterUSE, key, String.valueOf(timestamp),
                                        consistencyType);
                                break;
                            case 2:
                                response = KeyValueLib.GET(dataCenterUSW, key, String.valueOf(timestamp),
                                        consistencyType);
                                break;
                            case 3:
                                response = KeyValueLib.GET(dataCenterSING, key, String.valueOf(timestamp),
                                        consistencyType);
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        req.response().end(response);
                        req.response().close();
                    }
                });
                t.start();
            }
        });
        /*
         * This endpoint is used by the grader to change the consistency level
         */
        routeMatcher.get("/consistency", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                MultiMap map = req.params();
                consistencyType = map.get("consistency");
                req.response().end();
                req.response().close();
            }
        });
        routeMatcher.noMatch(new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                req.response().putHeader("Content-Type", "text/html");
                String response = "Not found.";
                req.response().putHeader("Content-Length", String.valueOf(response.length()));
                req.response().end(response);
                req.response().close();
            }
        });
        server.requestHandler(routeMatcher);
        server.listen(8080);
    }
}
