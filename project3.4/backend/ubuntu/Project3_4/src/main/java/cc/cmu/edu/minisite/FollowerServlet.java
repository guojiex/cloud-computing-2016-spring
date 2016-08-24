package cc.cmu.edu.minisite;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;

import org.json.JSONObject;

import org.json.JSONArray;

public class FollowerServlet extends HttpServlet {

    /**
     * The private IP address of HBase master node.
     */
    private static String zkAddr = "172.31.23.32";
    /**
     * The name of your HBase table.
     */
    private static String tableName = "q2data";
    /**
     * HTable handler.
     */
    private static HTableInterface linksTable;
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

    public FollowerServlet() throws IOException {
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
        linksTable = conn.getTable(Bytes.toBytes(tableName));

    }

    public class Follower implements Comparable<Follower> {
        String name;
        String profileUrl;

        /**
         * @param name
         * @param profileUrl
         */
        public Follower(String name, String profileUrl) {
            super();
            this.name = name;
            this.profileUrl = profileUrl;
        }

        @Override
        public int compareTo(Follower o) {
            if (this.name.equals(o.name)) {
                return this.profileUrl.compareTo(o.profileUrl);
            } else {
                return this.name.compareTo(o.name);
            }
        }

    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        String id = request.getParameter("id");
        // JSONObject result = new JSONObject();

        /*
         * Task 2: Implement your logic to retrive the followers of this user.
         * You need to send back the Name and Profile Image URL of his/her
         * Followers.
         * 
         * You should sort the followers alphabetically in ascending order by
         * Name. If there is a tie in the followers name, sort alphabetically by
         * their Profile Image URL in ascending order.
         */
        JSONObject result = new JSONObject();
        List<Integer> userIdList = new ArrayList<Integer>();
        Get get = new Get(Bytes.toBytes(id));
        Result resultSet = linksTable.get(get);
        for (KeyValue kv :  resultSet.list()) {
            String users=Bytes.toString(kv.getValue());
            for(String user:users.split(" ")){
                if(!user.isEmpty()){
                    System.out.println(user);
                    userIdList.add(Integer.parseInt(user));
                }
            }
        }
        PriorityQueue<Follower> queue = getFollowers(userIdList);

        
        JSONArray followers = new JSONArray();
        while (!queue.isEmpty()) {
            Follower followerInstance=queue.poll();
            JSONObject follower = new JSONObject();
            follower.put("name", followerInstance.name);
            follower.put("profile", followerInstance.profileUrl);
            followers.put(follower);
        }
        result.put("followers", followers);
        PrintWriter writer = response.getWriter();
        writer.write(String.format("returnRes(%s)", result.toString()));
        writer.close();
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private String URL = "jdbc:mysql://mydbinstance.ckbemsncikro.us-east-1.rds.amazonaws.com:3306/" + DB_NAME;
    private static final String DB_NAME = "mydb";
    private static final String DB_USER = "root";
    private static final String DB_PWD = "db15319root";

    public PriorityQueue<Follower> getFollowers(List<Integer> userIdList) {
        PriorityQueue<Follower> queue = new PriorityQueue<Follower>();
        ResultSet rs = null;
        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(URL,
                    DB_USER, DB_PWD);
            for (Integer userId : userIdList) {
                String stringsql = String.format("select profileurl,name from userinfo where userid=%d ", userId);
                Statement stmt = conn.createStatement();
                rs = stmt.executeQuery(stringsql);
                while (rs.next()) {
                    queue.offer(new Follower(rs.getString("name"), rs.getString("profileurl")));
                }
                stmt.close();
            }
            rs.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return queue;
    }
}
