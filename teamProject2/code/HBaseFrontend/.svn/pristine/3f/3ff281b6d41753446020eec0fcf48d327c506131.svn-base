package servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Q2Request
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/q2" })
public class Q2Request extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String AWS_ACCOUNT_ID = "505243408493";
    private static final String TEAM_NAME = "MyLittlePony";
    private final static int CACHE_SIZE = 20000;
    private Map<String, String> longRequestCache = new HashMap<>();

    private HBaseRequester requester = null;

    private static LinkedHashMap<String, String> cache = new LinkedHashMap<String, String>(CACHE_SIZE, (float) 0.75,
            true) {
        private static final long serialVersionUID = 1941528571731979451L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    @Override
    public void init() {
        try {
            requester = new HBaseRequester();
            genNewSchema();
            // initLongRequestMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void genNewSchema() throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(new File("allUniqInput")));
        String readLine = null;
        while ((readLine = in.readLine()) != null) {
            String[] items = readLine.split("\\t");
            String userid = items[0];
            String hashtag = items[1];
            System.out.println(userid + "_" + hashtag + "\t" + requester.getResponse2(userid, hashtag));
        }
        in.close();
    }

    private void initLongRequestMap() throws Exception {
        // InputStream longRequestFile = Q2Request.class.getResourceAsStream("tweetLog");
        BufferedReader in = new BufferedReader(new FileReader(new File("tweetLog")));
        String readLine = null;
        while ((readLine = in.readLine()) != null) {
            String[] items = readLine.split("&");
            longRequestCache.put(readLine, requester.getResponse(items[0], items[1]));
        }
        in.close();
        // System.out.println("longRequestCache initialize complete");

    }

    private long totalTime = 0;
    private int numOfRequest = 0;
    private int numOfLong = 0;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String userid = request.getParameter("userid");
        String hashtag = request.getParameter("hashtag");

        String target = String.format("%s&%s", userid, hashtag);

        String result = null;

        if (longRequestCache.containsKey(target)) {
            result = longRequestCache.get(target);
        } else if (cache.containsKey(target)) {
            result = cache.get(target);
        }

        if (result == null) {
            numOfRequest++;
            long startTime = System.currentTimeMillis();
            result = String.format("%s,%s\n%s\n", TEAM_NAME, AWS_ACCOUNT_ID,
                    requester.getResponse(userid, hashtag));

            long timeDiff = (System.currentTimeMillis() - startTime);
            totalTime += timeDiff;
            if (timeDiff > 120) {
                numOfLong++;
                // System.out.println(target);
                // System.out.println("DB Query Time: " + timeDiff);
                // System.out.println("average: " + (totalTime / numOfRequest));
                // System.out.println("numOfLong:" + numOfLong);
            }

            cache.put(target, result);
        }
        out.print(result);

        if (userid == null || hashtag == null) {
            out.print("parameter missing");
            return;
        }

    }

}
