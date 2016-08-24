package cc.cmu.edu.minisite;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import cc.cmu.edu.minisite.TimelineServlet.Follower;
import cc.cmu.edu.minisite.TimelineServlet.Post;

public class RecommendationServlet extends HttpServlet {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private String URL = "jdbc:mysql://mydbinstance.ckbemsncikro.us-east-1.rds.amazonaws.com:3306/" + DB_NAME;
    private static final String DB_NAME = "mydb";
    private static final String DB_USER = "root";
    private static final String DB_PWD = "db15319root";
    /**
     * The private IP address of HBase master node.
     */
    private static String zkAddr = "172.31.23.32";

    /**
     * HTable handler.
     */
    private static HTableInterface linksq4Table;
    /**
     * HBase connection.
     */
    private static HConnection conn;
    /**
     * Byte representation of column family.
     */
    private static byte[] bColFamily = Bytes.toBytes("data");
    /**
     * Logger.
     */
    private final static Logger logger = Logger.getRootLogger();

    public RecommendationServlet() throws Exception {
        logger.setLevel(Level.ERROR);
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.master", zkAddr + ":60000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        conf.set("hbase.zookeeper.property.clientport", "2181");
        if (!zkAddr.matches("\\d+.\\d+.\\d+.\\d+")) {
            System.out.print("HBase not configured!");
            return;
        }
        conn = HConnectionManager.createConnection(conf);
        linksq4Table = conn.getTable(Bytes.toBytes("q4data"));
    }
    public class ScoreUser implements Comparable<ScoreUser> {
        int userId;
        int score;
        
        /**
         * @param userId
         * @param score
         */
        public ScoreUser(int userId, int score) {
            super();
            this.userId = userId;
            this.score = score;
        }

        @Override
        public int compareTo(ScoreUser arg0) {
            if(score==arg0.score){
                return userId-arg0.userId;
            }else{
                return -(score-arg0.score);
            }
        }
        
    }
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject result = new JSONObject();
        String id = request.getParameter("id");

        /**
         * Bonus task:
         * 
         * Recommend at most 10 people to the given user with simple
         * collaborative filtering.
         * 
         * Store your results in the result object in the following JSON format:
         * recommendation: [ {name:<name_1>, profile:<profile_1>} {name:
         * <name_2>, profile:<profile_2>} {name:<name_3>, profile:<profile_3>}
         * ... {name:<name_10>, profile:<profile_10>} ]
         * 
         * Notice: make sure the input has no duplicate!
         */
        List<Integer> userIdList = new ArrayList<Integer>();
        Get get = new Get(Bytes.toBytes(id));
        Result resultSet = linksq4Table.get(get);
        for (KeyValue kv : resultSet.list()) {
            String users = Bytes.toString(kv.getValue());
            for (String user : users.split(" ")) {
                if (!user.isEmpty()) {
                    userIdList.add(Integer.parseInt(user));
                }
            }
        }
        HashMap<Integer, Integer> scoreMap = new HashMap<Integer, Integer>();
        for (Integer userId : userIdList) {
            //System.out.println(userId);
            Get get2 = new Get(Bytes.toBytes(String.valueOf(userId)));
            Result resultSet2 = linksq4Table.get(get2);
            //if (resultSet2.list() != null)
                for (KeyValue kv : resultSet2.list()) {
                    String users = Bytes.toString(kv.getValue());
                    for (String user : users.split(" ")) {
                        if (!user.isEmpty()) {
                            int tempUser = Integer.parseInt(user);
                            if (scoreMap.containsKey(tempUser)) {
                                scoreMap.put(tempUser, scoreMap.get(tempUser) + 1);
                            } else {
                                scoreMap.put(tempUser, 1);
                            }
                        }
                    }
                }
        }
        final PriorityQueue<ScoreUser> queue = new PriorityQueue<ScoreUser>();
        for (Integer key : scoreMap.keySet()) {
            if (!key.equals(Integer.parseInt(id)) && !userIdList.contains(key))
                queue.add(new ScoreUser(key,scoreMap.get(key)));
        }
        JSONArray recommendations = new JSONArray();
        int count = 0;
        ResultSet rs = null;
        String profileUrl = null;
        String userName = null;
        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);
            while(!queue.isEmpty()){
                String stringsql = String.format(
                        "select profileurl,name from userinfo,users where userinfo.userid=%s AND users.userid=%s",
                        queue.peek().userId, queue.peek().userId);
                queue.poll();
                Statement stmt = conn.createStatement();
                rs = stmt.executeQuery(stringsql);
                while (rs.next()) {
                    profileUrl = rs.getString("profileurl");
                    userName = rs.getString("name");
                }
                JSONObject one = new JSONObject();
                one.put("name", userName);
                one.put("profile", profileUrl);
                recommendations.put(one);
                count++;
                if (count == 10) {
                    rs.close();
                    stmt.close();
                    conn.close();
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        result.put("recommendation", recommendations);
        PrintWriter writer = response.getWriter();
        writer.write(String.format("returnRes(%s)", result.toString()));
        writer.close();

    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
