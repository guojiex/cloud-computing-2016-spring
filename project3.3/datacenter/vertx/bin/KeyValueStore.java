import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

public class KeyValueStore extends Verticle {

    private static ConcurrentHashMap<String, String> storageMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> timeStampMap = new ConcurrentHashMap<>();
    private Map<String, PriorityQueue<Long>> operations = new ConcurrentHashMap<>();

    @Override
    public void start() {
        final KeyValueStore keyValueStore = new KeyValueStore();
        final RouteMatcher routeMatcher = new RouteMatcher();
        final HttpServer server = vertx.createHttpServer();
        server.setAcceptBacklog(32767);
        server.setUsePooledBuffers(true);
        server.setReceiveBufferSize(4 * 1024);
        routeMatcher.get("/put", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                MultiMap map = req.params();
                String key = map.get("key");
                String value = map.get("value");
                String consistency = map.get("consistency");
                Integer region = Integer.parseInt(map.get("region"));
                Long timestamp = Long.parseLong(map.get("timestamp"));
                if (operations.get(key) == null) {
                    PriorityQueue<Long> queue = new PriorityQueue<Long>();
                    KeyValueStore.this.operations.put(key, queue);
                    timeStampMap.put(key, timestamp);
                }
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        if (consistency.equals("strong")) {
                            PriorityQueue<Long> queue = operations.get(key);
                            synchronized (queue) {
                                //System.out.println("put " + key + " " + timestamp);
                                while (queue.isEmpty() || !queue.peek().equals(timestamp)) {
                                    //System.out.println("put wait " + timestamp + " " + key + " " + queue.peek());
                                    try {
                                        queue.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                storageMap.put(key, value);
                                queue.notifyAll();
                            }
                        } else {
                            synchronized (storageMap) {
                                if (!storageMap.containsKey(key)) {
                                    storageMap.put(key, value);
                                    timeStampMap.put(key, timestamp);
                                } else {
                                    if (timeStampMap.get(key) < timestamp) {
                                        storageMap.put(key, value);
                                        timeStampMap.put(key, timestamp);
                                    }
                                }
                            }
                        }
                        String response = "stored";
                        req.response().putHeader("Content-Type", "text/plain");
                        req.response().putHeader("Content-Length", String.valueOf(response.length()));
                        req.response().end(response);
                        req.response().close();
                    }
                });
                t.start();
            }
        });
        routeMatcher.get("/get", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                MultiMap map = req.params();
                final String key = map.get("key");
                String consistency = map.get("consistency");
                final Long timestamp = Long.parseLong(map.get("timestamp"));
                if (operations.get(key) == null) {
                    PriorityQueue<Long> queue = new PriorityQueue<Long>();
                    KeyValueStore.this.operations.put(key, queue);
                    timeStampMap.put(key, timestamp);
                }
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        if (consistency.equals("strong")) {
                            synchronized (operations) {
                                if (operations.get(key) == null) {
                                    PriorityQueue<Long> queue = new PriorityQueue<Long>();
                                    KeyValueStore.this.operations.put(key, queue);
                                }
                                KeyValueStore.this.operations.get(key).add(timestamp);
                            }
                            PriorityQueue<Long> queue = KeyValueStore.this.operations.get(key);
                            String response = "";
                            synchronized (queue) {
                                //System.out.println("get " + key + " " + timestamp);
                                while (queue.isEmpty() || !queue.peek().equals(timestamp)) {
                                    //System.out.println("get wait " + timestamp + " " + key + " " + queue.peek());
                                    try {
                                        queue.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }        
                                response = KeyValueStore.this.storageMap.get(key);
                                queue.poll();
                                queue.notifyAll();
                            }
                            req.response().putHeader("Content-Type", "text/plain");
                            if (response != null) {
                                req.response().putHeader("Content-Length", String.valueOf(response.length()));
                                req.response().end(response);
                                req.response().close();
                            } else {
                                req.response().end("");
                                req.response().close();
                            }
                        } else {
                            //System.out.println("get "+key+" "+timestamp);
                            String response = null;
                            response = KeyValueStore.storageMap.get(key);

                            req.response().putHeader("Content-Type", "text/plain");
                            if (response != null) {
                                req.response().putHeader("Content-Length", String.valueOf(response.length()));
                                req.response().end(response);
                                req.response().close();
                            } else {
                                req.response().end("0");
                                req.response().close();
                            }
                        }
                    }
                });
                t.start();
            }
        });
        // Clears this stored keys. Do not change this
        routeMatcher.get("/reset", new Handler<HttpServerRequest>() {

            @Override
            public void handle(final HttpServerRequest req) {
                KeyValueStore.storageMap.clear();
                KeyValueStore.this.operations.clear();
                KeyValueStore.this.timeStampMap.clear();
                //System.out.println("clear.");
                req.response().putHeader("Content-Type", "text/plain");
                req.response().end();
                req.response().close();
            }
        });
        // Handler for when the AHEAD is called
        routeMatcher.get("/ahead", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                MultiMap map = req.params();
                String key = map.get("key");
                final Long timestamp = Long.parseLong(map.get("timestamp"));

                if (operations.get(key) == null) {
                    PriorityQueue<Long> queue = new PriorityQueue<Long>();
                    KeyValueStore.this.operations.put(key, queue);
                }
                PriorityQueue<Long> queue = KeyValueStore.this.operations.get(key);
                queue.add(timestamp);
                req.response().putHeader("Content-Type", "text/plain");
                req.response().end();
                req.response().close();
            }
        });
        // Handler for when the COMPLETE is called
        routeMatcher.get("/complete", new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest req) {
                MultiMap map = req.params();
                String key = map.get("key");
                final Long timestamp = Long.parseLong(map.get("timestamp"));
                PriorityQueue<Long> queue = KeyValueStore.this.operations.get(key);
                synchronized (queue) {
                    queue.poll();
                    queue.notifyAll();
                }
                req.response().putHeader("Content-Type", "text/plain");
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
