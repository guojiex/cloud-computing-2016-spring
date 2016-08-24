package undertow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;

/**
 * @author wyen Wei-Yu Yen.
 */
public class ConnectionPool {
    private static final String zkAddr = "172.31.7.51";

    private static final int NUM_OF_INIT_CONN = 20;

    private static List<HConnection> connPool = new ArrayList<>();

    static {
        for (int i = 0; i < NUM_OF_INIT_CONN; i++) {
            try {
                connPool.add(createConnection());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ConnectionPool() {

    }

    public static synchronized HConnection getConnection() throws IOException {
        if (connPool.isEmpty()) {
            System.out.println("create new connection!");
            return createConnection();
        } else {
            return connPool.remove(connPool.size() - 1);
        }
    }

    public static synchronized void releaseConnection(HConnection conn) {
        connPool.add(conn);
    }

    private static HConnection createConnection() throws IOException {
        return HConnectionManager.createConnection(getHBaseConfiguration());
    }

    public static Configuration getHBaseConfiguration() {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.master", zkAddr + ":60000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        conf.set("hbase.zookeeper.property.clientport", "2181");
        return conf;
    }
}
