import org.vertx.java.core.Handler;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class MSB extends Verticle {
    private final int CACHE_SIZE = 1000;
    private LinkedHashMap<Integer, String> cache = new LinkedHashMap<>();
    private String[] databaseInstances = new String[2];

    /**
     * Once fetch a new cache, we fetch CACHE_BLOCK_SIZE lines.
     */
    private final int CACHE_BLOCK_SIZE = 25;

    /*
     * init -initializes the variables which store the DNS of your database
     * instances
     */
    private void init() {
        /* Add the DNS of your database instances here */
        databaseInstances[0] = "ec2-54-175-69-54.compute-1.amazonaws.com";
        databaseInstances[1] = "ec2-52-90-251-102.compute-1.amazonaws.com";
        cache.clear();
    }

    /*
     * checkBackend - verifies that the DCI are running before starting this
     * server
     */
    private boolean checkBackend() {
        try {
            if (sendRequest(generateURL(0, "1")) == null || sendRequest(generateURL(1, "1")) == null)
                return true;
        } catch (Exception ex) {
            System.out.println("Exception is " + ex);
        }

        return false;
    }

    /*
     * sendRequest Input: URL Action: Send a HTTP GET request for that URL and
     * get the response Returns: The response
     */
    private String sendRequest(String requestUrl) throws Exception {

        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

        String responseCode = Integer.toString(connection.getResponseCode());
        if (responseCode.startsWith("2")) {
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            System.out.println("Unable to connect to " + requestUrl
                    + ". Please check whether the instance is up and also the security group settings");
            return null;
        }
    }

    /*
     * generateURL Input: Instance ID of the Data Center id Returns: URL which
     * can be used to retrieve the user's details from the data center instance
     * Additional info: the user's details are cached on backend instance
     */
    private String generateURL(Integer instanceID, String key) {
        return "http://" + databaseInstances[instanceID] + "/target?targetID=" + key;
    }

    /*
     * generateRangeURL Input: Instance ID of the Data Center startRange -
     * starting range (id) endRange - ending range (id) Returns: URL which can
     * be used to retrieve the details of all user in the range from the data
     * center instance Additional info: the details of the last 1000 user are
     * cached in the database instance
     * 
     */
    private String generateRangeURL(Integer instanceID, String startRange, String endRange) {
        return "http://" + databaseInstances[instanceID] + "/range?start_range=" + (startRange) + "&end_range="
                + (endRange);
    }

    /**
     * Get a cache line by targetID
     * 
     * @param targetID
     * @return
     */
    private String getCacheById(String targetID) {
        return this.cache.get(Integer.parseInt(targetID));
    }

    /**
     * Update local cache from start id to end id.
     * 
     * @param start
     * @param end
     */
    private void updateLocalCache(String start, String end) {
        int cache_block_number = (int) Math
                .ceil((Integer.parseInt(end) - Integer.parseInt(start)) / (double) this.CACHE_BLOCK_SIZE);
        end = String.valueOf(Integer.parseInt(start) + cache_block_number * this.CACHE_BLOCK_SIZE - 1);
        String response = null;
        try {
            response = sendRequest(generateRangeURL(lastIndex, start, end));
            lastIndex = lastIndex == 1 ? 0 : 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] lines = response.split(";");
        int i = Integer.parseInt(start);
        int index = 0;
        int size = lines.length;
         int toBeRemoved = this.cache.size() + size - this.CACHE_SIZE;
        Iterator<Integer> iterator = cache.keySet().iterator();
        while (toBeRemoved > 0) {
            iterator.next();
            iterator.remove();
            toBeRemoved--;
        }

        while (index < size) {
            this.cache.put(i, lines[index]);
            i++;
            index++;
        }
        //System.out.println(cache.size());
        //this.ensureCacheSizeLessThanLimit();
    }

    /**
     * delete entries that exceeds the cache limit.
     */
    private void ensureCacheSizeLessThanLimit() {
        Iterator<Integer> iterator = cache.keySet().iterator();
        while (this.cache.size() > this.CACHE_SIZE) {
            iterator.next();
            iterator.remove();
        }
        // System.out.println("cache size:" + this.cache.size());
    }

    /**
     * The index of last DC that send the request to .
     */
    private int lastIndex = 0;

    /*
     * retrieveDetails - you have to modify this function to achieve a higher
     * RPS value Input: the targetID Returns: The result from querying the
     * database instance
     */
    private String retrieveDetails(String targetID) {
        String response = null;
        try {
            response = this.getCacheById(targetID);
            if (response != null) {
                return response;
            }
            this.updateLocalCache(targetID);
            response = this.getCacheById(targetID);
            if (response != null) {
                return response;
            }
            return null;
        } catch (Exception ex) {
            System.out.println(ex);

            return null;
        }
    }

    /**
     * Update local cache from start id to one block size
     * 
     * @param start
     */
    private void updateLocalCache(String start) {
        int cache_block_number = 1;

        String end = String.valueOf(Integer.parseInt(start) + cache_block_number * this.CACHE_BLOCK_SIZE - 1);
        String response = null;
        try {
            response = sendRequest(generateRangeURL(lastIndex, start, end));
            lastIndex = lastIndex == 1 ? 0 : 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] lines = response.split(";");
        int i = Integer.parseInt(start);
        int index = 0;
        int size = lines.length;
        int toBeRemoved = this.cache.size() + size - this.CACHE_SIZE;
        Iterator<Integer> iterator = cache.keySet().iterator();
        while (toBeRemoved > 0) {
            iterator.next();
            iterator.remove();
            toBeRemoved--;
        }
        while (index < size) {
            this.cache.put(i, lines[index]);
            i++;
            index++;
        }
        //System.out.println(this.cache.size());
        //this.ensureCacheSizeLessThanLimit();
    }

    private String retrieveDetails(String start, String end) {
        String response = null;
        try {
            response = this.getCacheById(start, end);
            if (response != null) {// if cached
                return response;
            }
            this.updateLocalCache(start, end);// if not cached
            response = this.getCacheById(start, end);
            if (response != null) {
                return response;
            }
            lastIndex = lastIndex == 1 ? 0 : 1;

            return response;
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
    }

    /**
     * Get cache lines from start to end
     * 
     * @param start
     * @param end
     * @return
     */
    private String getCacheById(String start, String end) {
        if (!this.cache.containsKey(Integer.parseInt(end)) || !this.cache.containsKey(Integer.parseInt(start))) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = Integer.parseInt(start); i <= Integer.parseInt(end); ++i) {
            sb.append(this.cache.get(String.valueOf(i))).append(";");
        }
        return sb.toString();
    }

    /*
     * processRequest - calls the retrieveDetails function with the id
     */
    private void processRequest(String id, HttpServerRequest req) {
        String result = retrieveDetails(id);
        if (result != null)
            req.response().end(result);
        else
            req.response().end("No resopnse received");
    }

    private void processRequest(String start, String end, HttpServerRequest req) {
        String result = retrieveDetails(start, end);
        if (result != null)
            req.response().end(result);
        else
            req.response().end("No resopnse received");
    }

    /*
     * start - starts the server
     */
    public void start() {
        init();
        if (!checkBackend()) {
            vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
                public void handle(HttpServerRequest req) {
                    String query_type = req.path();
                    req.response().headers().set("Content-Type", "text/plain");
                    if (query_type.equals("/target")) {
                        String key = req.params().get("targetID");
                        processRequest(key, req);
                    } else if (query_type.equals("/range")) {
                        String start = req.params().get("start_range");
                        String end = req.params().get("end_range");
                        processRequest(start, end, req);
                    }
                }
            }).listen(80);
        } else {
            System.out.println("Please make sure that both your DCI are up and running");
        }
    }
}
