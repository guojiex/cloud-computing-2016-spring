package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class HBaseRequester {

    /**
     * The private IP address of HBase master node.
     */
    private static final String zkAddr = "172.31.13.250";
    /**
     * The name of your HBase table.
     */
    private static final String tableName = "tweetdata";
    /**
     * HTable handler.
     */
    private static HTableInterface tweetTable;
    /**
     * Byte representation of column family.
     */
    private static final byte[] bColFamily = Bytes.toBytes("data");

    private List<HConnection> connPool = new ArrayList<>();

    private static final int NUM_OF_INIT_CONN = 12;

    public HBaseRequester() throws ZooKeeperConnectionException {
        for (int i = 0; i < NUM_OF_INIT_CONN; i++) {
            connPool.add(createConnection());
        }
    }

    private synchronized HConnection getConnection() throws ZooKeeperConnectionException {
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

    private HConnection createConnection() throws ZooKeeperConnectionException {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.master", zkAddr + ":60000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        conf.set("hbase.zookeeper.property.clientport", "2181");
        if (!zkAddr.matches("\\d+.\\d+.\\d+.\\d+")) {
            System.out.print("HBase not configured!");
            return null;
        }

        return HConnectionManager.createConnection(conf);
    }

    public class Response implements Comparable<Response> {
        double sentimentDensity = 0;
        String tweetTime;
        String tweetId;
        String censoredText;

        public Response(String tweetTime, String tweetId, String censoredText, String sentimentDensityStr) {
            this.censoredText = censoredText;// TODO remove this comment.replaceAll("\\\\n", "\n");
            this.sentimentDensity = Double.parseDouble(sentimentDensityStr);
            this.tweetTime = tweetTime;
            this.tweetId = tweetId;
        }

        @Override
        public String toString() {
            return String.format("%.3f:%s:%s:%s", sentimentDensity,
                    this.tweetTime, this.tweetId, this.censoredText);
        }

        @Override
        public int compareTo(Response o) {
            if (this.sentimentDensity != o.sentimentDensity) {
                return (int) (o.sentimentDensity - this.sentimentDensity);
            } else if (!tweetTime.equals(o.tweetTime)) {
                return tweetTime.compareTo(o.tweetTime);
            } else {
                return tweetId.compareTo(o.tweetId);
            }
        }
    }

    public String getResponse(String userid, String hashtag) throws IOException {
        // initializeConnection();
        // System.out.println("1");
        HConnection conn = getConnection();
        // System.out.println("2");
        tweetTable = conn.getTable(Bytes.toBytes(tableName));
        // System.out.println("3");

        Scan scan = new Scan();
        byte[] tweetIdCol = Bytes.toBytes("tweet_id");
        byte[] tweetTimeCol = Bytes.toBytes("date");
        byte[] censoredTextCol = Bytes.toBytes("censored_text");
        byte[] sentimentDensityCol = Bytes.toBytes("sentiment_density");
        byte[] hashtagsCol = Bytes.toBytes("hashtags");

        scan.addColumn(bColFamily, tweetIdCol);
        scan.addColumn(bColFamily, tweetTimeCol);
        scan.addColumn(bColFamily, censoredTextCol);
        scan.addColumn(bColFamily, sentimentDensityCol);
        scan.addColumn(bColFamily, hashtagsCol);

        userid = userid + "_";

        scan.setStartRow(Bytes.toBytes(userid));
        FilterList filterList = new FilterList();
        filterList.addFilter(new PrefixFilter(Bytes.toBytes(userid)));
        filterList.addFilter(new SingleColumnValueFilter(bColFamily, hashtagsCol, CompareFilter.CompareOp.EQUAL,
                new RegexStringComparator(".*" + hashtag + "(;|$)")));
        scan.setFilter(filterList);
        scan.setBatch(100);
        ResultScanner rs = tweetTable.getScanner(scan);

        PriorityQueue<Response> queue = new PriorityQueue<>();

        for (Result r = rs.next(); r != null; r = rs.next()) {
            queue.offer(new Response(Bytes.toString(r.getValue(bColFamily, tweetTimeCol)),
                    Bytes.toString(r.getValue(bColFamily, tweetIdCol)),
                    Bytes.toString(r.getValue(bColFamily, censoredTextCol)),
                    Bytes.toString(r.getValue(bColFamily, sentimentDensityCol))));
        }
        rs.close();
        tweetTable.close();
        releaseConnection(conn);
        // cleanup();

        StringBuffer sb = new StringBuffer();
        if (queue.isEmpty())
            return "\n";
        while (!queue.isEmpty()) {
            sb.append(queue.poll()).append("\n");
        }

        return sb.toString();
    }

    public String getResponse2(String userid, String hashtag) throws IOException {
        // initializeConnection();
        // System.out.println("1");
        HConnection conn = getConnection();
        // System.out.println("2");
        tweetTable = conn.getTable(Bytes.toBytes(tableName));
        // System.out.println("3");

        Scan scan = new Scan();
        byte[] tweetIdCol = Bytes.toBytes("tweet_id");
        byte[] tweetTimeCol = Bytes.toBytes("date");
        byte[] censoredTextCol = Bytes.toBytes("censored_text");
        byte[] sentimentDensityCol = Bytes.toBytes("sentiment_density");
        byte[] hashtagsCol = Bytes.toBytes("hashtags");

        scan.addColumn(bColFamily, tweetIdCol);
        scan.addColumn(bColFamily, tweetTimeCol);
        scan.addColumn(bColFamily, censoredTextCol);
        scan.addColumn(bColFamily, sentimentDensityCol);
        scan.addColumn(bColFamily, hashtagsCol);

        userid = userid + "_";

        scan.setStartRow(Bytes.toBytes(userid));
        FilterList filterList = new FilterList();
        filterList.addFilter(new PrefixFilter(Bytes.toBytes(userid)));
        filterList.addFilter(new SingleColumnValueFilter(bColFamily, hashtagsCol, CompareFilter.CompareOp.EQUAL,
                new RegexStringComparator(".*" + hashtag + "(;|$)")));
        scan.setFilter(filterList);
        scan.setBatch(100);
        ResultScanner rs = tweetTable.getScanner(scan);

        PriorityQueue<Response> queue = new PriorityQueue<>();

        for (Result r = rs.next(); r != null; r = rs.next()) {
            queue.offer(new Response(Bytes.toString(r.getValue(bColFamily, tweetTimeCol)),
                    Bytes.toString(r.getValue(bColFamily, tweetIdCol)),
                    Bytes.toString(r.getValue(bColFamily, censoredTextCol)),
                    Bytes.toString(r.getValue(bColFamily, sentimentDensityCol))));
        }
        rs.close();
        tweetTable.close();
        releaseConnection(conn);
        // cleanup();

        StringBuffer sb = new StringBuffer();
        if (queue.isEmpty())
            return "\\\\n";
        while (!queue.isEmpty()) {
            sb.append(queue.poll()).append("\\\\n");
        }

        return sb.toString();
    }

    // /**
    // * Clean up resources.
    // * @throws IOException
    // */
    // private static void cleanup() throws IOException {
    // if (tweetTable != null) {
    // tweetTable.close();
    // }
    // if (conn != null) {
    // conn.close();
    // }
    // }
}
