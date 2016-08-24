package server;

import java.net.UnknownHostException;

import javax.servlet.ServletException;

import org.apache.hadoop.hbase.ZooKeeperConnectionException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import servlet.MyHandler;

/**
 * @author wyen Wei-Yu Yen.
 */
public class UndertowServer {
    public static final String MYAPP = "/";

    /**
     * @param args
     * @throws UnknownHostException
     * @throws ServletException
     * @throws ZooKeeperConnectionException
     */
    public static void main(String[] args) throws UnknownHostException, ServletException, ZooKeeperConnectionException {
        Undertow server;
        HttpHandler handler = new MyHandler();
        PathHandler path = Handlers
                .path(Handlers.redirect(MYAPP))
                .addPrefixPath(MYAPP, handler);
        
        server = Undertow.builder().setServerOption(UndertowOptions.IDLE_TIMEOUT, 600000)
                .setServerOption(UndertowOptions.REQUEST_PARSE_TIMEOUT, 600000)
                .setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT, 600000)
                .addHttpListener(80, "0.0.0.0").setHandler(path).setIoThreads(20).build();

        server.start();
       
    }

}
