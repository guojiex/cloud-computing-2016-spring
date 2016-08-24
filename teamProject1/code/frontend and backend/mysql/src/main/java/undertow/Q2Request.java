package undertow;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Q2Request,
 * Abandoned
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/q2" })
public class Q2Request extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String AWS_ACCOUNT_ID = "505243408493";
    private static final String TEAM_NAME = "MyLittlePony";
    private static Map<String, Integer> AFINNDataset = null;
    private static Set<String> stopWordSet = null;
    private static Set<String> censorMap = null;
    private static HashMap<Character, Character> lookupTable = null;

    


    /**
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws MalformedURLException
     * @see HttpServlet#HttpServlet()
     */
    public Q2Request() throws ClassNotFoundException, SQLException, MalformedURLException {
        super();
        
        requester = new MysqlRequester("localhost", this.censorMap, this.lookupTable, this.stopWordSet, AFINNDataset);
        //cache.clear();
    }

    private final static int CACHE_SIZE = 10000;

    private static LinkedHashMap<String, String> cache = new LinkedHashMap<String, String>(CACHE_SIZE, (float) 0.75,
            true);

    /**
     * Get a cache line by targetID
     * 
     * @param targetID
     * @return
     */
    private String getCacheById(String targetID) {
        return Q2Request.cache.get(targetID);
    }

    private void insertCache(String targetID, String line) {
        Q2Request.cache.put(targetID, line);
        Iterator<String> iterator = cache.keySet().iterator();
        while (Q2Request.cache.size() > Q2Request.CACHE_SIZE) {
            iterator.next();
            iterator.remove();
        }
    }

    MysqlRequester requester = null;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String userid = request.getParameter("userid");
        String hashtag = request.getParameter("hashtag");

        if (userid == null || hashtag == null) {
            out.print("parameter missing");
            return;
        }
        // Connection conn=null;
        // try {
        // Class.forName(JDBC_DRIVER);
        // } catch (ClassNotFoundException e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        // try {
        // conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);
        // } catch (SQLException e1) {
        // e1.printStackTrace();
        // }
        String target = String.format("%s&%s", userid, hashtag);
        String result = this.getCacheById(target);
        if (result != null) {
            out.print(result);
        } else {
            try {
                String line = String.format("%s,%s\n%s\n", TEAM_NAME, AWS_ACCOUNT_ID,
                        requester.getResponse(userid, hashtag));
                if(line!=null&&line.isEmpty())
                this.insertCache(target, line);
                out.print(line);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
    }

}
