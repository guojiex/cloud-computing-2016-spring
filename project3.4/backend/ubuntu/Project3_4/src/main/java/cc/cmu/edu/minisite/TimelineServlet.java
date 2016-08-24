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
import java.util.Stack;

import javax.servlet.ServletConfig;
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
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import cc.cmu.edu.minisite.FollowerServlet.Follower;

public class TimelineServlet extends HttpServlet {
    private static final String MongoDBDNS = "ec2-52-71-255-182.compute-1.amazonaws.com";
    private MongoClient mongoClient;
    private MongoDatabase db;
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
     * The name of your HBase table.
     */
    private static String tableName = "q2data";
    /**
     * HTable handler.
     */
    private static HTableInterface linksTable;
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

    public TimelineServlet() throws Exception {
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
        linksq4Table = conn.getTable(Bytes.toBytes("q4data"));
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

    public class Post implements Comparable<Post> {
        String timestamp;
        Integer pid;

        JSONObject thing;

        public Post(JSONObject jsonObject) {
            thing = jsonObject;
            timestamp = thing.getString("timestamp");
            pid = thing.getInt("pid");
        }

        @Override
        public int compareTo(Post arg0) {
            if (this.timestamp.equals(arg0.timestamp)) {
                return -this.pid.compareTo(arg0.pid);
            } else {
                return -this.timestamp.compareTo(arg0.timestamp);
            }
        }

    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        mongoClient = new MongoClient(MongoDBDNS, 27017);
        db = mongoClient.getDatabase("test");

        JSONObject result = new JSONObject();
        String id = request.getParameter("id");

        /*
         * Task 4 (1): Get the name and profile of the user as you did in Task 1
         * Put them as fields in the result JSON object
         */
        ResultSet rs = null;
        String profileUrl = null;
        String userName = null;
        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);
            String stringsql = String.format(
                    "select profileurl,name from userinfo,users where userinfo.userid=%s AND users.userid=%s", id, id);
            Statement stmt = conn.createStatement();

            rs = stmt.executeQuery(stringsql);
            while (rs.next()) {
                profileUrl = rs.getString("profileurl");
                userName = rs.getString("name");
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        result.put("name", userName);
        result.put("profile", profileUrl);
        /*
         * Task 4 (2); Get the follower name and profiles as you did in Task 2
         * Put them in the result JSON object as one array
         */
        List<Integer> userIdList = new ArrayList<Integer>();
        Get get = new Get(Bytes.toBytes(id));
        Result resultSet = linksTable.get(get);
        for (KeyValue kv : resultSet.list()) {
            String users = Bytes.toString(kv.getValue());
            for (String user : users.split(" ")) {
                if (!user.isEmpty()) {
                    userIdList.add(Integer.parseInt(user));
                }
            }
        }
        PriorityQueue<Follower> queue = getFollowers(userIdList);

        JSONArray followers = new JSONArray();
        while (!queue.isEmpty()) {
            Follower followerInstance = queue.poll();
            JSONObject follower = new JSONObject();
            follower.put("name", followerInstance.name);
            follower.put("profile", followerInstance.profileUrl);
            followers.put(follower);
        }
        result.put("followers", followers);
        /*
         * Task 4 (3): Get the 30 LATEST followee posts and put them in the
         * result JSON object as one array.
         * 
         * The posts should be sorted: First in ascending timestamp order Then
         * numerically in ascending order by their PID (PostID) if there is a
         * tie on timestamp
         */
        mongoClient = new MongoClient(MongoDBDNS, 27017);
        db = mongoClient.getDatabase("test");
        JSONArray posts = new JSONArray();
        Get get2 = new Get(Bytes.toBytes(id));
        Result resultSet2 = linksq4Table.get(get2);
        final PriorityQueue<Post> queue2 = new PriorityQueue<Post>();
        for (KeyValue kv : resultSet2.list()) {
            String users = Bytes.toString(kv.getValue());
            for (String user : users.split(" ")) {
                if (!user.isEmpty()) {
                    FindIterable<Document> iterable = db.getCollection("posts")
                            .find(new Document("uid", Integer.parseInt(user)));
                    iterable.forEach(new Block<Document>() {
                        @Override
                        public void apply(final Document document) {
                            queue2.offer(new Post(new JSONObject(document.toJson())));
                        }
                    });
                }
            }
        }
        mongoClient.close();
        Stack<JSONObject> temp=new Stack<JSONObject>();
        for (int i = 0; i < 30; i++) {
            if (queue2.isEmpty()) {
                break;
            } else {
                temp.push(queue2.poll().thing);
            }
        }
        while(!temp.isEmpty()){
            posts.put(temp.pop());
        }
        result.put("posts", posts);
        PrintWriter out = response.getWriter();
        out.print(String.format("returnRes(%s)", result.toString()));
        out.close();
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    public PriorityQueue<Follower> getFollowers(List<Integer> userIdList) {
        PriorityQueue<Follower> queue = new PriorityQueue<Follower>();
        ResultSet rs = null;
        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);
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
