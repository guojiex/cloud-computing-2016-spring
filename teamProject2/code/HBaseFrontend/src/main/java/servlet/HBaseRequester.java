package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class HBaseRequester {

    /**
     * The private IP address of HBase master node.
     */
    private static final String zkAddr = "172.31.13.8";
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

    private final static int CACHE_SIZE = 10000;

    private static LinkedHashMap<String, String> cache = new LinkedHashMap<String, String>(CACHE_SIZE, (float) 0.75,
            true) {
        private static final long serialVersionUID = 1941528571731979451L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > CACHE_SIZE;
        }
    };

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
            this.censoredText = censoredText.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\\\"");
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
                return (o.sentimentDensity - this.sentimentDensity) > 0 ? 1 : -1;
            } else if (!tweetTime.equals(o.tweetTime)) {
                return tweetTime.compareTo(o.tweetTime);
            } else {
                return tweetId.compareTo(o.tweetId);
            }
        }
    }

    public static final String SPLIT = "%#\\$@%#";
    public static final String SEPERATOR = "@#@%%@";

    public String getResponse(String userid, String hashtag) throws IOException {
        HConnection conn = getConnection();
        tweetTable = conn.getTable(Bytes.toBytes(tableName));
        String valueStr = null;

        if (cache.containsKey(userid)) {
            valueStr = cache.get(userid);
        } else {
            // Get g = new Get(Bytes.toBytes(userid + hashtag));
            Get g = new Get(Bytes.toBytes(userid));

            Result record = tweetTable.get(g);
            byte[] value = record.getValue(bColFamily, Bytes.toBytes("value"));
            valueStr = Bytes.toString(value);
            cache.put(userid, valueStr);
        }
        String[] texts = valueStr.split(SPLIT);

        PriorityQueue<Response> queue = new PriorityQueue<>();

        // for (String text : texts) {
        // String[] items = text.split(":");
        // queue.offer(new Response(items[1], items[2], text.substring(text.indexOf(items[3])), items[0]));
        // }
        for (String text : texts) {
            String[] rec = text.split(SEPERATOR);
            if (rec[0].indexOf(hashtag) > -1) {
                String[] items = rec[1].split(":");
                queue.offer(new Response(items[1], items[2], text.substring(text.indexOf(items[3])), items[0]));
            }
        }

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

}
