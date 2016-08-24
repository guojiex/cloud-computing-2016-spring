package undertow;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MysqlRequester {

     private static Connection conn = null;

    private Map<String, Integer> AFINNDataset = null;
    private Set<String> stopWordSet = null;
    private Set<String> censorMap = null;
    private HashMap<Character, Character> lookupTable = null;// WashData.createLookupTable();

    public MysqlRequester(String mysqlUrl, Set<String> censorMap2, HashMap<Character, Character> lookupTable2,
            Set<String> stopWordSet2, Map<String, Integer> AFINNDataset2)
                    throws MalformedURLException, ClassNotFoundException, SQLException {
        this.lookupTable = lookupTable2;
        this.censorMap = censorMap2;
        this.stopWordSet = stopWordSet2;
        this.AFINNDataset = AFINNDataset2;

//         if (MysqlRequester.conn == null) {
//         Class.forName(JDBC_DRIVER);
//         conn = DriverManager.getConnection(URL +
//         "?useUnicode=true&characterEncoding=utf-8&useSSL=false", DB_USER,
//         DB_PWD);
//         conn.setAutoCommit(true);
//         conn.setReadOnly(true);
//         }
        // Statement stmt=conn.createStatement();
        // stmt.execute("start transaction read only;");
    }

    public static String replaceAllCharExceptFirstAndLast(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s.charAt(0));
        for (int i = 1; i < s.length() - 1; ++i)
            sb.append("*");
        sb.append(s.charAt(s.length() - 1));
        return sb.toString();
    }

    private String censorText(String text) {
        Pattern p = Pattern.compile("[^a-zA-Z0-9]");
        Matcher m = p.matcher(text);
        int start = 0;
        int end = 0;
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (m.start() - start > 0) {
                String temp = text.substring(start, m.start());
                if (censorMap.contains(temp.toLowerCase())) {
                    sb.append(replaceAllCharExceptFirstAndLast(temp));
                } else {
                    sb.append(temp);
                }
            }
            sb.append(text.substring(m.start(), m.end()));
            start = m.end();
            end = m.end();
        }
        if (end < text.length()) {
            String temp = text.substring(end);
            if (censorMap.contains(temp.toLowerCase())) {
                sb.append(replaceAllCharExceptFirstAndLast(temp));
            } else {
                sb.append(temp);
            }
        }
        return sb.toString();
    }

    private int getEffectiveWordCount(String[] words) {
        int count = 0;
        int wordCount = 0;
        for (String word : words) {
            if (word.matches("[a-zA-Z0-9]+")) {
                wordCount++;
            }
            if (this.stopWordSet.contains(word.toLowerCase())) {
                count++;
            }
        }
        return wordCount - count;
    }

    private String getSentimentDensity(String text) {
        String[] words = text.split("[^a-zA-Z0-9]");
        int score = 0;
        for (String word : words) {
            if (this.AFINNDataset.containsKey(word.toLowerCase())) {
                score += this.AFINNDataset.get(word.toLowerCase());
            }
        }
        int EWC = this.getEffectiveWordCount(words);
        double result = (EWC == 0 ? 0 : score / (double) EWC);
        return String.format("%.3f", result);
    }

    public class Response implements Comparable<Response> {
        double Sentiment_density = 0;
        String Tweet_time;
        String Tweet_id;
        String Cencored_text;
        String censored;

        public Response(String string, String string2, String string3) {
            String temp = string3.replaceAll("\\\\n", "\n");
            Sentiment_density = Double.parseDouble(getSentimentDensity(temp));
            Tweet_time = string;
            Tweet_id = string2;
            this.censored = censorText(temp);
        }

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

    public String getResponse(String userid, String hashtag)
            throws ClassNotFoundException, SQLException, UnsupportedEncodingException {
        Class.forName(JDBC_DRIVER);
        Connection conn = DriverManager.getConnection(URL + "?useUnicode=true&characterEncoding=utf-8&useSSL=false",
                DB_USER, DB_PWD);
        conn.setAutoCommit(true);
        conn.setReadOnly(true);
        StringBuilder sb = new StringBuilder();

        PriorityQueue<Response> queue = new PriorityQueue<>();
        String stringsql = "select hashtags,rawtext from tweetdata where userid=?";
        java.sql.PreparedStatement ps = conn.prepareStatement(stringsql);
        ps.setString(1, userid);
        // String sql = "SELECT hashtags,tweetdate,tweetid,rawtext FROM " +
        // tableName + " WHERE userid='" + userid + "'";
        ResultSet rs = null;
        try {
            // stmt = conn.createStatement();
            // rs = stmt.executeQuery(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String[] hashtags = rs.getString("hashtags").split(";");
                for (String hashtag2 : hashtags) {
                    if (hashtag2 != null && !hashtag2.isEmpty() && hashtag2.equals(hashtag)) {
                        // Response one = new
                        // Response(rs.getString("tweetdate"),
                        // rs.getBigDecimal("tweetid").toBigIntegerExact().toString(),
                        // rs.getString("rawtext"));
                        Response one = new Response(rs.getString("rawtext"));
                        queue.offer(one);
                        continue;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                rs.close();
            }
            conn.close();
        }
        if (queue.isEmpty())
            return "\n";
        while (!queue.isEmpty()) {
            sb.append(queue.poll()).append("\n");
        }
        return sb.toString();
    }
}
