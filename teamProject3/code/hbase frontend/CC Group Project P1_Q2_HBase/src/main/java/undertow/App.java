package undertow;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;

public class App {

    public static final String ROOTPATH = "/";
    public static final String Q2ROUTE = "/q2";
    public static final String Q1ROUTE = "/q1";
    public static final String Q3ROUTE = "/q3";
    public static final String Q4ROUTE = "/q4";
    public static final String HEALTHCHECKROUTE = "/healthcheck";

    public static void main(String[] args) {

        Undertow server;
        try {
            HttpHandler q1handler = new Q1Handler();
            HttpHandler q2handler = new Q2Handler();
            HttpHandler q3handler = new Q3Handler();
            HttpHandler q4handler = new Q4Handler(Integer.valueOf(args[0]));
            PathHandler path = Handlers
                    .path(Handlers.redirect(ROOTPATH))
                    .addPrefixPath(Q1ROUTE, q1handler)
                    .addPrefixPath(Q2ROUTE, q2handler)
                    .addPrefixPath(Q3ROUTE, q3handler)
                    .addPrefixPath(Q4ROUTE, q4handler);
            server = Undertow.builder()/* .setIoThreads(16).setWorkerThreads(16) */
                    .addHttpListener(90, "0.0.0.0").setHandler(path).build();

            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
