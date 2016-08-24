package undertow;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.dbcp.BasicDataSource;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class Q3Handler implements HttpHandler {
    private static final String AWS_ACCOUNT_ID = "505243408493";
    private static final String TEAM_NAME = "MyLittlePony";
    private static BasicDataSource bds = null;
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_NAME = "tweet_db";
    private static final String DB_USER = "root";
    private static final String DB_PWD = "db15319root";
    private final static SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyyMMdd");
    private final static TimeZone pst = TimeZone.getTimeZone("Etc/GMT+0");

    public Q3Handler() {
        if (bds == null) {
            bds = new BasicDataSource();
            bds.setDriverClassName(JDBC_DRIVER);
            bds.setUsername(DB_USER);
            bds.setPassword(DB_PWD);
            bds.setUrl(
                    "jdbc:mysql://localhost:3306/" + DB_NAME + "?useUnicode=true&characterEncoding=utf-8&useSSL=false");
        }
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
         if (exchange.isInIoThread()) {
         exchange.dispatch(this);
         return;
         }

        if (exchange == null || exchange.getQueryParameters().get("start_date") == null
                || exchange.getQueryParameters().get("end_date") == null
                || exchange.getQueryParameters().get("start_userid") == null
                || exchange.getQueryParameters().get("end_userid") == null
                || exchange.getQueryParameters().get("words") == null) {
            exchange.getResponseSender().send("q3 parameter missing");
            return;
        }
        String startDate = exchange.getQueryParameters().get("start_date").peek();
        String endDate = exchange.getQueryParameters().get("end_date").peek();
        String startUserId = exchange.getQueryParameters().get("start_userid").peek();
        String endUserId = exchange.getQueryParameters().get("end_userid").peek();
        String words = exchange.getQueryParameters().get("words").peek();
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty() || startUserId == null
                || startUserId.isEmpty() || endUserId == null || endUserId.isEmpty() || words == null
                || words.isEmpty()) {
            exchange.getResponseSender().send("q3 parameter missing");
            return;
        }

        String line = String.format("%s,%s\n%s", TEAM_NAME, AWS_ACCOUNT_ID,
                getResponse(startDate, endDate, startUserId, endUserId, words));
        exchange.getResponseSender().send(line);
        return;

    }

    private static String getDate(String date) {
        return date.replaceAll("-", "");

    }

    private static final String stringsql = "select wordcount from q3data where userid>=%s and userid<=%s and datetime>=%s and datetime<=%s";
    // private static final String stringsql = "select wordcount from q3data
    // where useriddate>=%s%s and useriddate<=%s%s";

    // private static final String stringsql = "select wordcount from q3data
    // where userid between %s and %s and datetime between %s and %s";
    public String getResponse(String startDate, String endDate, String startUserId, String endUserId, String words) {
        String[] threeWords = words.split(",");
        ArrayList<String> list = new ArrayList<>();
        for (String word : threeWords) {
            list.add(word.toLowerCase());
        }
        int[] counts = new int[threeWords.length];
        Connection conn = null;
        ResultSet rs = null;
        Statement stmt=null;
        StringBuilder sb = new StringBuilder();
        try {
            conn = this.bds.getConnection();
             stmt= conn.createStatement();
 
            rs = stmt.executeQuery(
                    String.format(stringsql, startUserId, endUserId, getDate(startDate), getDate(endDate)));
            while (rs.next()) {
                String[] wordCounts = rs.getString(1).split(",");
                for (String wordCount : wordCounts) {
                    String[] word = wordCount.split(":");
                    if (list.contains(word[0])) {
                        counts[list.indexOf(word[0])] += Integer.parseInt(word[1]);
                    }
                }
            }
           
            for (int i = 0; i < counts.length; i++) {
                sb.append(String.format("%s:%d\n", threeWords[i], counts[i]));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                rs.close();
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
