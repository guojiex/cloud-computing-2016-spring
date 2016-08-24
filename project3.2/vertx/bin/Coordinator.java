import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.sql.Timestamp;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

public class Coordinator extends Verticle {

    // Default mode: sharding. Possible string values are "replication" and
    // "sharding"
    private static String storageType = "replication";

    public static class Operation {
        public enum Type {
            PUT, GET
        };

        public Type t;
        public String timestamp;
        public String index;

        public Operation(Type type, String time, String location) {
            this.t = type;
            this.timestamp = time;
            this.index = location;
        }
    }

    /**
     * TODO: Set the values of the following variables to the DNS names of your
     * three dataCenter instances
     */
    private static final String dataCenter1 = "ec2-54-172-233-110.compute-1.amazonaws.com";
    private static final String dataCenter2 = "ec2-54-173-53-206.compute-1.amazonaws.com";
    private static final String dataCenter3 = "ec2-54-173-230-85.compute-1.amazonaws.com";
    private static HashMap<String, PriorityQueue<Operation>> map = new HashMap<>();
    private static HashMap<String, PriorityQueue<Operation>> map11 = new HashMap<>();
    private static HashMap<String, PriorityQueue<Operation>> map12 = new HashMap<>();
    private static HashMap<String, PriorityQueue<Operation>> map13 = new HashMap<>();

    public PriorityQueue<Operation> createEmptyPriorityQueue() {
        PriorityQueue<Operation> queue = new PriorityQueue<Operation>(10, operationComparator);
        return queue;
    }

    public static Comparator<Operation> operationComparator = new Comparator<Operation>() {
        @Override
        public int compare(Operation o1, Operation o2) {
            return o1.timestamp.compareTo(o2.timestamp);
        }
    };

    public static int myHash(String key) {
        int res = 0;
        for (int i = 0; i < key.length(); i++) {
            res += Math.abs(key.charAt(i) - 'a') ;
        }
        return res % 3;
    }

    @Override
    public void start() {
        // DO NOT MODIFY THIS
        KeyValueLib.dataCenters.put(dataCenter1, 1);
        KeyValueLib.dataCenters.put(dataCenter2, 2);
        KeyValueLib.dataCenters.put(dataCenter3, 3);
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
                // You may use the following timestamp for ordering requests
                final String timestamp = new Timestamp(
                        System.currentTimeMillis() + TimeZone.getTimeZone("EST").getRawOffset()).toString();
                Thread t = new Thread(new Runnable() {
                    public void forReplication() {
                        Operation current = new Operation(Operation.Type.PUT, timestamp, null);
                        synchronized (Coordinator.this.map) {
                            if (!Coordinator.this.map.containsKey(key)) {
                                Coordinator.this.map.put(key, createEmptyPriorityQueue());
                                PriorityQueue<Operation> queue = Coordinator.this.map.get(key);
                                queue.offer(current);
                            } else {
                                PriorityQueue<Operation> queue = Coordinator.this.map.get(key);
                                queue.offer(current);
                            }
                        }
                        PriorityQueue<Operation> queue = Coordinator.this.map.get(key);
                        synchronized (queue) {
                            while (!(queue.peek() == current)) {
                                try {
                                    queue.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (queue.peek() == current) {
                                queue.poll();
                                try {
                                    KeyValueLib.PUT(dataCenter1, key, value);
                                    KeyValueLib.PUT(dataCenter2, key, value);
                                    KeyValueLib.PUT(dataCenter3, key, value);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                queue.notifyAll();
                            }
                        }
                    }

                    public void forSharding() {
                        Operation current = new Operation(Operation.Type.PUT, timestamp, null);
                        int index = myHash(key);
                        HashMap<String, PriorityQueue<Operation>> tempMap = null;
                        switch (index) {
                        case 0:
                            tempMap = Coordinator.map11;
                            break;
                        case 1:
                            tempMap = Coordinator.map12;
                            break;
                        case 2:
                            tempMap = Coordinator.map13;
                            break;
                        }
                        PriorityQueue<Operation> queue = null;
                        // synchronized (tempMap) {
                        if (!tempMap.containsKey(key)) {
                            tempMap.put(key, createEmptyPriorityQueue());
                        }
                        queue = tempMap.get(key);
                        queue.offer(current);
                        // }
                        synchronized (queue) {
                            while (!(queue.peek() == current)) {
                                try {
                                    queue.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (queue.peek() == current) {
                                queue.poll();
                                try {
                                    switch (index) {
                                    case 0:
                                        KeyValueLib.PUT(dataCenter1, key, value);
                                        break;
                                    case 1:
                                        KeyValueLib.PUT(dataCenter2, key, value);
                                        break;
                                    case 2:
                                        KeyValueLib.PUT(dataCenter3, key, value);
                                        break;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                queue.notifyAll();
                            }
                        }
                    }

                    public void run() {
                        if (storageType.equals("replication")) {
                            this.forReplication();
                        } else {
                            this.forSharding();
                        }
                        // TODO: Write code for PUT operation here.
                        // Each PUT operation is handled in a different thread.
                        // Highly recommended that you make use of helper
                        // functions.
                    }
                });
                t.start();
                req.response().end(); // Do not remove this
            }
        });

        routeMatcher.get("/get", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                MultiMap map = req.params();
                final String key = map.get("key");
                final String loc = map.get("loc");
                // You may use the following timestamp for ordering requests
                final String timestamp = new Timestamp(
                        System.currentTimeMillis() + TimeZone.getTimeZone("EST").getRawOffset()).toString();
                Thread t = new Thread(new Runnable() {
                    public void forReplication() {
                        Operation current = new Operation(Operation.Type.GET, timestamp, null);
                        synchronized (Coordinator.this.map) {
                            if (!Coordinator.this.map.containsKey(key)) {
                                Coordinator.this.map.put(key, createEmptyPriorityQueue());
                                PriorityQueue<Operation> queue = Coordinator.this.map.get(key);
                                queue.offer(current);
                            } else {
                                PriorityQueue<Operation> queue = Coordinator.this.map.get(key);
                                queue.offer(current);
                            }
                        }
                        PriorityQueue<Operation> queue = Coordinator.this.map.get(key);
                        synchronized (queue) {
                            while (!(queue.peek() == current)) {
                                try {
                                    queue.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (queue.peek() == current) {
                                queue.poll();
                                try {
                                    req.response().end(KeyValueLib.GET(dataCenter1, key));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                queue.notifyAll();
                            }
                        }
                    }

                    public void forSharding() {
                        Operation current = new Operation(Operation.Type.GET, timestamp, loc);
                        HashMap<String, PriorityQueue<Operation>> tempMap = null;
                        int index = myHash(key);
                        if (loc.equals("1")) {
                            tempMap = Coordinator.map11;
                        } else if (loc.equals("2")) {
                            tempMap = Coordinator.map12;
                        } else if (loc.equals("3")) {
                            tempMap = Coordinator.map13;
                        } else {
                            switch (index) {
                            case 0:
                                tempMap = Coordinator.map11;
                                break;
                            case 1:
                                tempMap = Coordinator.map12;
                                break;
                            case 2:
                                tempMap = Coordinator.map13;
                                break;
                            }
                        }

                        PriorityQueue<Operation> queue = null;
                        // synchronized (tempMap) {
                        if (!tempMap.containsKey(key)) {
                            tempMap.put(key, createEmptyPriorityQueue());
                        }
                        queue = tempMap.get(key);
                        queue.offer(current);
                        // }
                        synchronized (queue) {
                            while (!(queue.peek() == current)) {
                                try {
                                    queue.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (queue.peek() == current) {
                                queue.poll();
                                if (current.index != null) {
                                    index = Integer.parseInt(current.index);
                                    try {
                                        switch (index) {
                                        case 1:
                                            req.response().end(KeyValueLib.GET(dataCenter1, key));
                                            break;
                                        case 2:
                                            req.response().end(KeyValueLib.GET(dataCenter2, key));
                                            break;
                                        case 3:
                                            req.response().end(KeyValueLib.GET(dataCenter3, key));
                                            break;
                                        default:
                                            req.response().end("0");
                                            break;
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    queue.notifyAll();
                                    return;
                                }
                                try {
                                    switch (index) {
                                    case 0:
                                        req.response().end(KeyValueLib.GET(dataCenter1, key));
                                        break;
                                    case 1:
                                        req.response().end(KeyValueLib.GET(dataCenter2, key));
                                        break;
                                    case 2:
                                        req.response().end(KeyValueLib.GET(dataCenter3, key));
                                        break;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                queue.notifyAll();
                            }
                        }
                    }

                    public void run() {
                        // TODO: Write code for GET operation here.
                        // Each GET operation is handled in a different thread.
                        // Highly recommended that you make use of helper
                        // functions.
                        if (storageType.equals("replication")) {
                            this.forReplication();
                        } else {
                            this.forSharding();
                        }
                        // req.response().end("0"); // Default response = 0
                    }
                });
                t.start();
            }
        });

        routeMatcher.get("/storage", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                MultiMap map = req.params();
                storageType = map.get("storage");
                // This endpoint will be used by the auto-grader to set the
                // consistency type that your key-value store has to support.
                // You can initialize/re-initialize the required data structures
                // here
                Coordinator.map.clear();
                Coordinator.map11.clear();
                Coordinator.map12.clear();
                Coordinator.map13.clear();
                req.response().end();
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
