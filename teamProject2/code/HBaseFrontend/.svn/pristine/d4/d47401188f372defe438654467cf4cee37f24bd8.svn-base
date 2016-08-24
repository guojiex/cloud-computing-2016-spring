package servlet;

import java.io.IOException;
import java.io.PrintWriter;
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
    private final static int CACHE_SIZE = 5000000;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long totalTime = 0;
    private int numOfRequest = 0;

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

        if (cache.containsKey(target)) {
            result = cache.get(target);
        }

        if (result == null) {
            numOfRequest++;
            long startTime = System.currentTimeMillis();
            result = String.format("%s,%s\n%s\n", TEAM_NAME, AWS_ACCOUNT_ID,
                    requester.getResponse(userid, hashtag));

            long timeDiff = (System.currentTimeMillis() - startTime);
            totalTime += timeDiff;
            System.out.println(target);
            System.out.println("DB Query Time: " + timeDiff);
            // System.out.println("average: " + (totalTime / (double) numOfRequest));

            cache.put(target, result);
        }
        out.print(result);

        if (userid == null || hashtag == null) {
            out.print("parameter missing");
            return;
        }

    }

}
