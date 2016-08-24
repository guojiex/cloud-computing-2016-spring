package undertow;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class Q2HBaseRequester {

    private static final String tableName = "tweetdata";

    private static final byte[] bColFamily = Bytes.toBytes("data");

    private final static int CACHE_SIZE = 1000;

    private static LinkedHashMap<String, String> cache = new MyLinkedHashMap<String, String>(CACHE_SIZE, (float) 0.75,
            true) {
        private static final long serialVersionUID = -5172267633594967292L;

        @Override
        protected boolean removeEldestEntry(Entry<String, String> eldest) {
            return this.size() > CACHE_SIZE;
        }
    };

    public Q2HBaseRequester() throws IOException {
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
        String valueStr = null;

        if (cache.containsKey(userid)) {
            valueStr = cache.get(userid);
        } else {
            HConnection conn = ConnectionPool.getConnection();
            HTableInterface tweetTable = conn.getTable(Bytes.toBytes(tableName));
            Get g = new Get(Bytes.toBytes(userid));

            Result record = tweetTable.get(g);
            byte[] value = record.getValue(bColFamily, Bytes.toBytes("value"));
            valueStr = Bytes.toString(value);
            // cache.put(userid, valueStr);
            tweetTable.close();
            ConnectionPool.releaseConnection(conn);
        }

        if (valueStr == null) {
            return "";
        }

        String[] texts = valueStr.split(SPLIT);

        PriorityQueue<Response> queue = new PriorityQueue<>();

        for (String text : texts) {
            String[] rec = text.split(SEPERATOR);
            if (rec[0].indexOf(hashtag) > -1) {
                String[] items = rec[1].split(":");
                queue.offer(new Response(items[1], items[2], text.substring(text.indexOf(items[3])), items[0]));
            }
        }

        StringBuffer sb = new StringBuffer();
        if (queue.isEmpty())
            return "\n";
        while (!queue.isEmpty()) {
            sb.append(queue.poll()).append("\n");
        }

        return sb.toString();
    }

}
