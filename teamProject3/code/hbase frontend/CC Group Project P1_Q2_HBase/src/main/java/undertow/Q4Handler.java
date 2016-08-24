package undertow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
//import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.DatatypeConverter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.sun.jersey.core.util.Base64;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * @author wyen Wei-Yu Yen.
 */
public class Q4Handler implements HttpHandler {
    private static final String[] DNSs = {
            "ec2-52-201-231-3.compute-1.amazonaws.com", "ec2-54-89-67-229.compute-1.amazonaws.com" };
    private static int myIndex = 1;
    private static final String AWS_ACCOUNT_ID = "505243408493";
    private static final String TEAM_NAME = "MyLittlePony";

    private static final String TABLE_NAME = "q4tweet";
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private static final byte[] bColFamily = Bytes.toBytes("f");
    private static final Map<String, Object> orderList = new HashMap<>();

    public Q4Handler(int idx) {
        myIndex = idx;
        createTable();
    }

    private static String readFromUrl(String path) {
        URL oracle = null;
        URLConnection yc = null;
        BufferedReader in = null;
        StringBuilder sb = new StringBuilder();
        String inputLine = null;
        try {
            oracle = new URL(path);
            yc = oracle.openConnection();
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private static final int port = 90;
    public static byte[] decode2(String str){
        return str==null?null:DatatypeConverter.parseBase64Binary(str);
    }
    public static String encode2(byte[] bytes){
        return bytes==null?null:DatatypeConverter.printBase64Binary(bytes);
    }
    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.setDispatchExecutor(cachedThreadPool);
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        if (exchange == null || exchange.getQueryParameters().get("tweetid") == null
                || exchange.getQueryParameters().get("op") == null || exchange.getQueryParameters().get("seq") == null
                || exchange.getQueryParameters().get("fields") == null
                || exchange.getQueryParameters().get("payload") == null) {
            exchange.getResponseSender().send("q4 parameter missing");
            return;
        }

        final String tweetid = new StringBuilder(exchange.getQueryParameters().get("tweetid").peek()).reverse()
                .toString();
        final String op = exchange.getQueryParameters().get("op").peek();
        final String seq = exchange.getQueryParameters().get("seq").peek();
        final String fields = exchange.getQueryParameters().get("fields").peek();
        final String payload = exchange.getQueryParameters().get("payload").peek();
        int target = Math.abs(tweetid.hashCode()) % DNSs.length;
        if (target != myIndex) {
            String url = String.format("http://%s:%d/q4?%s", DNSs[target], port, exchange.getQueryString());
            exchange.getResponseSender().send(readFromUrl(url));
            return;
        }
        synchronized (orderList) {
            if (orderList.get(tweetid) == null) {
                orderList.put(tweetid, new Object());
            }
        }
        final Object obj = orderList.get(tweetid);
        if (!cache.containsKey(tweetid) || seq.equals("1"))
            cache.put(tweetid, 0);

        String response = "";

        synchronized (obj) {
            while (!cache.get(tweetid).equals(Integer.parseInt(seq) - 1)) {
                try {
                    obj.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if ("get".equals(op)) {
            String value = getResponse(tweetid, fields);
            if (value != null) {
                response = value.replaceAll(" ", "+");
            }
        }

        if ("set".equals(op)) {
            response = setValue(tweetid, fields.split(","), payload.replaceAll(" ", "+").split(","));
        }
        String line = String.format("%s,%s\n%s\n", TEAM_NAME, AWS_ACCOUNT_ID, response);

        exchange.getResponseSender().send(line);
        synchronized (obj) {
            cache.put(tweetid, Integer.parseInt(seq));
            obj.notifyAll();
        }
    }

    public String getResponse(String tweetid, String field) {
        HConnection conn = null;
        HTableInterface tweetTable = null;
        try {
            conn = ConnectionPool.getConnection();
            tweetTable = conn.getTable(Bytes.toBytes(TABLE_NAME));
            Get g = new Get(Bytes.toBytes(tweetid));
            Result record = tweetTable.get(g);
            byte[] value = record.getValue(bColFamily, Bytes.toBytes(field));
            return encode2(value);//Bytes.toString(value);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                tweetTable.close();
                ConnectionPool.releaseConnection(conn);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ConcurrentHashMap<String, Integer> cache = new ConcurrentHashMap<>();

    public String setValue(String tweetid, String[] fields, String[] payloads) {
        HConnection conn = null;
        HTableInterface tweetTable = null;
        try {
            conn = ConnectionPool.getConnection();
            tweetTable = conn.getTable(Bytes.toBytes(TABLE_NAME));
            int minLength = Math.min(fields.length, payloads.length);

            Put put = new Put(Bytes.toBytes(tweetid));
            for (int i = 0; i < minLength; i++) {
                put.add(bColFamily, Bytes.toBytes(fields[i]), decode2(payloads[i])/*Bytes.toBytes(payloads[i])*/);
            }

            tweetTable.put(put);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                tweetTable.close();
                ConnectionPool.releaseConnection(conn);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        return "success";
    }

    private void createTable() {
        Configuration conf = ConnectionPool.getHBaseConfiguration();

        HBaseAdmin admin = null;
        try {
            admin = new HBaseAdmin(conf);
            if (admin.tableExists(TABLE_NAME)) {
                System.out.println("table exists!recreating…….");
                admin.disableTable(TABLE_NAME);
                admin.deleteTable(TABLE_NAME);

                HTableDescriptor table = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
                HColumnDescriptor family = new HColumnDescriptor(bColFamily);
                family.setInMemory(true);
                table.addFamily(family);

                byte[][] regions = new byte[][] { Bytes.toBytes("1"), Bytes.toBytes("2"),
                        Bytes.toBytes("3"), Bytes.toBytes("4"), Bytes.toBytes("5"), Bytes.toBytes("6"),
                        Bytes.toBytes("7"), Bytes.toBytes("8"), Bytes.toBytes("9") };
                admin.createTable(table, regions);
                admin.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
