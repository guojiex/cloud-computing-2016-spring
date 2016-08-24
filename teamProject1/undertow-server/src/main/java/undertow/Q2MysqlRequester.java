package undertow;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.PriorityQueue;

import org.apache.commons.dbcp.BasicDataSource;


public class Q2MysqlRequester {
    private static BasicDataSource bds = null;

    public void setupDataSource() {
        if (bds == null) {
            bds = new BasicDataSource();
            bds.setDriverClassName(JDBC_DRIVER);
            bds.setUsername(DB_USER);
            bds.setPassword(DB_PWD);
            bds.setUrl(
                    "jdbc:mysql://localhost:3306/" + DB_NAME + "?useUnicode=true&characterEncoding=utf-8&useSSL=false");
        }
    }

    public void shutdownDataSource() throws SQLException {
        bds.close();
    }

    public Q2MysqlRequester(String mysqlUrl)
            throws MalformedURLException, ClassNotFoundException, SQLException {
        this.setupDataSource();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        this.shutdownDataSource();
        super.finalize();
    }

    public class Response implements Comparable<Response> {
        double Sentiment_density = 0;
        String Tweet_time;
        String Tweet_id;
        String Cencored_text;
        String censored;

        public Response(String string) {
            String temp = string.replaceAll("\\\\n", "\n");
            String[] word = temp.split(":");
            Sentiment_density = Double.parseDouble(word[0]);
            this.Tweet_time = word[1];
            this.Tweet_id = word[2];
            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < word.length; i++) {
                sb.append(word[i]);
                if (i != word.length - 1) {
                    sb.append(":");
                }
            }
            this.censored = sb.toString();
        }

        @Override
        public String toString() {
            return String.format("%.3f:%s:%s:%s", Sentiment_density, this.Tweet_time, this.Tweet_id, this.censored);
        }

        @Override
        public int compareTo(Response o) {
            if (this.Sentiment_density != o.Sentiment_density) {
                return (o.Sentiment_density - this.Sentiment_density) > 0 ? 1 : -1;
            } else if (!Tweet_time.equals(o.Tweet_time)) {
                return Tweet_time.compareTo(o.Tweet_time);
            } else {
                return Tweet_id.compareTo(o.Tweet_id);
            }
        }
    }

    String tableName = "tweetdata";
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private String URL = "jdbc:mysql://localhost/" + DB_NAME;
    private static final String DB_NAME = "tweet_db";
    private static final String DB_USER = "root";
    private static final String DB_PWD = "db15319root";
    //private static final String stringsql = "select tweetdata.rawtext from userhash,tweetdata where userhash.useridhash=? and userhash.rowkey=tweetdata.rowkey";
    private static final String stringsql = "select rawtext from tweetdata where useridhash='";
    public String getResponse(String userid, String hashtag)
            throws ClassNotFoundException, SQLException, UnsupportedEncodingException {

        Connection conn = null;

            conn = this.bds.getConnection();
       
        StringBuilder sb = new StringBuilder();

        PriorityQueue<Response> queue = new PriorityQueue<>();
        Statement stmt = conn.createStatement();
        //java.sql.PreparedStatement ps = conn.prepareStatement(stringsql);

        //ps.setString(1, String.format("%s%s", userid, hashtag));
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(stringsql+String.format("%s%s'", userid, hashtag));//ps.executeQuery();
            while (rs.next()) {
                Response one = new Response(rs.getString("rawtext"));
                queue.offer(one);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                rs.close();
            }
            conn.close();
            stmt.close();
        }
        if (queue.isEmpty())
            return "\n";
        while (!queue.isEmpty()) {
            sb.append(queue.poll()).append("\n");
        }
        return sb.toString();
    }
}
