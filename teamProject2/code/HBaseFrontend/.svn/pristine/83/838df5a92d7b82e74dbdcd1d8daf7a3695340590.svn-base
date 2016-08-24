package undertow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class Q3Handler implements HttpHandler {
    private static final String AWS_ACCOUNT_ID = "505243408493";
    private static final String TEAM_NAME = "MyLittlePony";

    private static final String zkAddr = "172.31.23.10";

    private static final String tableName = "wordcount";

    private static final byte[] bColFamily = Bytes.toBytes("data");

    private List<HConnection> connPool = new ArrayList<>();

    private static final int NUM_OF_INIT_CONN = 10;

    // private HTableInterface wordCountTable;

    public Q3Handler() throws IOException {
        for (int i = 0; i < NUM_OF_INIT_CONN; i++) {
            connPool.add(createConnection());
        }
        // wordCountTable = connPool.get(0).getTable(Bytes.toBytes(tableName));
    }

    private synchronized HConnection getConnection() throws IOException {
        if (connPool.isEmpty()) {
            System.out.println("create new connection!");
            return createConnection();
        } else {
            return connPool.remove(connPool.size() - 1);
        }
    }

    private synchronized void releaseConnection(HConnection conn) {
        connPool.add(conn);
    }

    private HConnection createConnection() throws IOException {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.master", zkAddr + ":60000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        conf.set("hbase.zookeeper.property.clientport", "2181");

        return HConnectionManager.createConnection(conf);
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

        String line = String.format("%s,%s\n%s", TEAM_NAME, AWS_ACCOUNT_ID,
                getResponse(startDate, endDate, startUserId, endUserId, words));
        exchange.getResponseSender().send(line);
        return;

    }

    private static String getDate(String date) {
        return date.replaceAll("-", "");
    }

    private String getFormatUserid(String userid, boolean isStart) {
        Long val = (isStart) ? Long.parseLong(userid) : Long.parseLong(userid) + 1;
        return String.format("%010d", val);
    }

    // private static final String stringsql = "select wordcount from q3data where userid between %s and %s and datetime
    // between %s and %s";
    public String getResponse(String startDate, String endDate, String startUserId, String endUserId, String words) {
        String[] threeWords = words.split(",");
        ArrayList<String> list = new ArrayList<>();
        for (String word : threeWords) {
            list.add(word.toLowerCase());
        }
        int[] counts = new int[threeWords.length];

        startDate = getDate(startDate);
        endDate = getDate(endDate);

        StringBuilder sb = new StringBuilder();
        try {
            HConnection conn = getConnection();
            HTableInterface wordCountTable = conn.getTable(Bytes.toBytes(tableName));

            Scan scan = new Scan();
            byte[] bCol = Bytes.toBytes("value");
            scan.addColumn(bColFamily, bCol);

            scan.setStartRow(Bytes.toBytes(getFormatUserid(startUserId, true)));
            scan.setStopRow(Bytes.toBytes(getFormatUserid(endUserId, false)));
            scan.setCaching(40);
            ResultScanner rs = wordCountTable.getScanner(scan);

            for (Result r = rs.next(); r != null; r = rs.next()) {
                String value = Bytes.toString(r.getValue(bColFamily, bCol));
                String[] datas = value.split("#");
                for (String data : datas) {
                    String[] items = data.split("@");
                    String date = items[0];
                    String wordsAndCounts = items[1];
                    if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0) {
                        String[] wordCounts = wordsAndCounts.split(",");
                        for (String wordCount : wordCounts) {
                            String[] word = wordCount.split(":");
                            if (list.contains(word[0])) {
                                counts[list.indexOf(word[0])] += Integer.parseInt(word[1]);
                            }
                        }
                    }
                }
            }
            releaseConnection(conn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < counts.length; i++) {
            sb.append(String.format("%s:%d\n", threeWords[i], counts[i]));
        }
        return sb.toString();
    }
}
