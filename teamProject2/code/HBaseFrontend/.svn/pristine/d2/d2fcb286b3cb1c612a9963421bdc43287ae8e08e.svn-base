package servlet;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.hadoop.hbase.ZooKeeperConnectionException;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class MyHandler implements HttpHandler {

    private HBaseRequester requester = null;

    private final static int CACHE_SIZE = 1000000;

    private static LinkedHashMap<String, String> cache = new LinkedHashMap<String, String>(CACHE_SIZE, (float) 0.75,
            true) {
        private static final long serialVersionUID = 1941528571731979451L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    /**
     * @param requester
     * @throws MalformedURLException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ZooKeeperConnectionException
     */
    public MyHandler() throws ZooKeeperConnectionException {
        requester = new HBaseRequester();
    }

    private static final String AWS_ACCOUNT_ID = "505243408493";
    private static final String TEAM_NAME = "MyLittlePony";

    @Override
    public void handleRequest(HttpServerExchange input) throws Exception {
        if (input == null || input.getQueryParameters().get("userid") == null
                || input.getQueryParameters().get("hashtag") == null) {
            input.getResponseSender().send("parameter missing");
            return;
        }
        String userid = input.getQueryParameters().get("userid").peek();
        String hashtag = input.getQueryParameters().get("hashtag").peek();
        input.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=UTF-8");

        String target = String.format("%s&%s", userid, hashtag);

        String result = null;

        if (cache.containsKey(target)) {
            result = cache.get(target);
        } else {

            // long startTime = System.currentTimeMillis();
            result = String.format("%s,%s\n%s\n", TEAM_NAME, AWS_ACCOUNT_ID,
                    requester.getResponse(userid, hashtag));

            // long timeDiff = (System.currentTimeMillis() - startTime);
            // System.out.println(target);
            // System.out.println("DB Query Time: " + timeDiff);
            // System.out.println("average: " + (totalTime / (double) numOfRequest));

            cache.put(target, result);
        }

        input.getResponseSender().send(result);
    }

}
