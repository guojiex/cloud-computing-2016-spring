import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SubstringComparator;

import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;

public class HBaseTasks {

    /**
     * The private IP address of HBase master node.
     */
    private static String zkAddr = "172.31.0.129";
    /**
     * The name of your HBase table.
     */
    private static String tableName = "songdata";
    /**
     * HTable handler.
     */
    private static HTableInterface songsTable;
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


    /**
     * Initialize HBase connection.
     * @throws IOException
     */
    private static void initializeConnection() throws IOException {
        // Remember to set correct log level to avoid unnecessary output.
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
        songsTable = conn.getTable(Bytes.toBytes(tableName));
    }

    /**
     * Clean up resources.
     * @throws IOException
     */
    private static void cleanup() throws IOException {
        if (songsTable != null) {
            songsTable.close();
        }
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * You should complete the missing parts in the following method. Feel free to add helper functions if necessary.
     *
     * For all questions, output your answer in ONE single line, i.e. use System.out.print().
     *
     * @param args The arguments for main method.
     */
    public static void main(String[] args) throws IOException {
        initializeConnection();
        switch (args[0]) {
            case "demo":
                demo();
                break;
            case "q17":
                q17();
                break;
            case "q18":
                q18();
                break;
            case "q19":
                q19();
                break;
            case "q20":
                q20();
                break;
            case "q21":
                q21();
        }
        cleanup();
    }

    /**
     * This is a demo of how to use HBase Java API. It will print all the artist_names starting with "The Beatles".
     * @throws IOException
     */
    private static void demo() throws IOException {
        Scan scan = new Scan();
        byte[] bCol = Bytes.toBytes("artist_name");
        scan.addColumn(bColFamily, bCol);
        RegexStringComparator comp = new RegexStringComparator("^The Beatles.*");
        Filter filter = new SingleColumnValueFilter(bColFamily, bCol, CompareFilter.CompareOp.EQUAL, comp);
        scan.setFilter(filter);
        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);
        int count = 0;
        for (Result r = rs.next(); r != null; r = rs.next()) {
            count ++;
            System.out.println(Bytes.toString(r.getValue(bColFamily, bCol)));
        }
        System.out.println("Scan finished. " + count + " match(es) found.");
        rs.close();
    }

    /**
     * Question 17.
     *
     * What was that song whose name started with "Total" and ended with "Water"?
     * Write an HBase query that finds the track that the person is looking for.
     * The title starts with "Total" and ends with "Water", both are case sensitive.
     * Print the track title(s) in a single line.
     *
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q17()throws IOException  {
        Scan scan = new Scan();

        byte[] bColFamily = Bytes.toBytes("data");

        byte[] queryCol = Bytes.toBytes("title");
        byte[] resultCol= Bytes.toBytes("title");
        RegexStringComparator comp = new RegexStringComparator("^Total.*Water$");

        Filter filter = new SingleColumnValueFilter(bColFamily, queryCol, CompareFilter.CompareOp.EQUAL, comp);

        scan.setFilter(filter);

        scan.setBatch(10);

        ResultScanner rs = songsTable.getScanner(scan);

        for (Result r = rs.next(); r != null; r = rs.next()) {
            System.out.println(Bytes.toString(r.getValue(bColFamily,resultCol)));
        }
    }

    /**
     * Question 18.
     *
     * I don't remember the exact title, it was that song by "Kanye West", and the
     * title started with either "Apologies" or "Confessions". Not sure which...
     * Write an HBase query that finds the track that the person is looking for.
     * The artist_name contains "Kanye West", and the title starts with either
     * "Apologies" or "Confessions" (Case sensitive).
     * Print the track title(s) in a single line.
     *
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q18()throws IOException  {
        Scan scan = new Scan();

        byte[] bColFamily = Bytes.toBytes("data");

        byte[] queryCol = Bytes.toBytes("title");
        byte[] queryCol2=Bytes.toBytes("artist_name");
        byte[] resultCol= Bytes.toBytes("title");
        RegexStringComparator comp = new RegexStringComparator("^Apologies.*|^Confessions.*");
        SubstringComparator comp2 = new SubstringComparator("Kanye West");
        FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);

        Filter filter = new SingleColumnValueFilter(bColFamily, queryCol, CompareFilter.CompareOp.EQUAL, comp);
        list.addFilter(filter);
        Filter filter2 = new SingleColumnValueFilter(bColFamily, queryCol2, CompareFilter.CompareOp.EQUAL, comp2);
        list.addFilter(filter2);

        scan.setFilter(list);
        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);

        for (Result r = rs.next(); r != null; r = rs.next()) {
            System.out.println(Bytes.toString(r.getValue(bColFamily,resultCol)));
        }

    }

    /**
     * Question 19.
     *
     * There was that new track by "Bob Marley" that was really long. Do you know?
     * Write an HBase query that finds the track the person is looking for.
     * The artist_name has a prefix of "Bob Marley", duration no less than 400,
     * and year 2000 and onwards (Case sensitive).
     * Print the track title(s) in a single line.
     *
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q19()throws IOException  {
        Scan scan = new Scan();

        byte[] bColFamily = Bytes.toBytes("data");
        byte[] queryCol2=Bytes.toBytes("artist_name");
        byte[] resultCol= Bytes.toBytes("title");
        RegexStringComparator comp2 = new RegexStringComparator("^Bob Marley.*");
        FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        RegexStringComparator comp4 = new RegexStringComparator("^[4-9]{1}[0-9]{2}");

        Filter filter = new SingleColumnValueFilter(bColFamily, "duration".getBytes(), 
                CompareFilter.CompareOp.GREATER_OR_EQUAL, comp4);
        list.addFilter(filter);
        Filter filter2 = new SingleColumnValueFilter(bColFamily, queryCol2, CompareFilter.CompareOp.EQUAL, comp2);
        list.addFilter(filter2);
        RegexStringComparator comp3 = new RegexStringComparator("^2[0-9]{3}$");

        list.addFilter(new SingleColumnValueFilter(bColFamily, 
                    "year".getBytes(), CompareFilter.CompareOp.EQUAL, comp3));
        scan.setFilter(list);

        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);

        for (Result r = rs.next(); r != null; r = rs.next()) {
            System.out.println(Bytes.toString(r.getValue(bColFamily,resultCol)));
        }
    }

    /**
     * Question 20.
     *
     * I heard a really great song about "Family" by this really cute singer,
     * I think his name was "Consequence" or something...
     * Write an HBase query that finds the track the person is looking for.
     * The track has an artist_hotttnesss of at least 1, and the artist_name
     * contains "Consequence". Also, the title contains "Family" (Case sensitive).
     * Print the track title(s) in a single line.
     *
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q20()throws IOException  {
        Scan scan = new Scan();
        byte[] bColFamily = Bytes.toBytes("data");
        byte[] resultCol= Bytes.toBytes("title");

        FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        list.addFilter(new SingleColumnValueFilter(bColFamily, 
                    "artist_hotttnesss".getBytes(), CompareFilter.CompareOp.GREATER_OR_EQUAL,"1".getBytes()));
        SubstringComparator comp = new SubstringComparator("Consequence");
        SubstringComparator comp2 = new SubstringComparator("Family");
        list.addFilter(new SingleColumnValueFilter(bColFamily, 
                    "artist_name".getBytes(), CompareFilter.CompareOp.EQUAL,comp));
        list.addFilter(new SingleColumnValueFilter(bColFamily, 
                    "title".getBytes(), CompareFilter.CompareOp.EQUAL,comp2));

        scan.setFilter(list);

        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);

        for (Result r = rs.next(); r != null; r = rs.next()) {
            System.out.println(Bytes.toString(r.getValue(bColFamily,resultCol)));
        }

    }

    /**
     * Question 21.
     *
     * Hey what was that "Love" song that "Gwen Guthrie" came out with in 1990?
     * No, no, it wasn't the sad one, nothing "Bitter" or "Never"...
     * Write an HBase query that finds the track the person is looking for.
     * The track has an artist_name prefix of "Gwen Guthrie", the title contains "Love"
     * but does NOT contain "Bitter" or "Never", the year equals to 1990.
     * Print the track title(s) in a single line.
     *
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q21()throws IOException  {
        Scan scan = new Scan();
        byte[] bColFamily = Bytes.toBytes("data");
        byte[] resultCol= Bytes.toBytes("title");

        FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);

        /*list.addFilter(new SingleColumnValueFilter(bColFamily, 
                    "artist_hotttnesss".getBytes(), CompareFilter.CompareOp.GREATER_OR_EQUAL,"1".getBytes()));*/
        RegexStringComparator comp = new RegexStringComparator("^Gwen Guthrie.*");
        SubstringComparator comp2 = new SubstringComparator("Love");
        SubstringComparator comp3 = new SubstringComparator("Bitter");
        SubstringComparator comp4 = new SubstringComparator("Never");

        list.addFilter(new SingleColumnValueFilter(bColFamily, 
                    "artist_name".getBytes(), CompareFilter.CompareOp.EQUAL,comp));

        list.addFilter(new SingleColumnValueFilter(bColFamily, 
                    "title".getBytes(), CompareFilter.CompareOp.EQUAL,comp2));
        list.addFilter(new SingleColumnValueFilter(bColFamily, 
                    "year".getBytes(), CompareFilter.CompareOp.EQUAL,"1990".getBytes()));
        list.addFilter(new SingleColumnValueFilter(bColFamily, 
                    "title".getBytes(), CompareFilter.CompareOp.NOT_EQUAL,comp3));
        list.addFilter(new SingleColumnValueFilter(bColFamily, 
                    "title".getBytes(), CompareFilter.CompareOp.NOT_EQUAL,comp4));

        scan.setFilter(list);

        scan.setBatch(10);
        ResultScanner rs = songsTable.getScanner(scan);

        for (Result r = rs.next(); r != null; r = rs.next()) {
            System.out.println(Bytes.toString(r.getValue(bColFamily,resultCol)));
        }

    }

}
