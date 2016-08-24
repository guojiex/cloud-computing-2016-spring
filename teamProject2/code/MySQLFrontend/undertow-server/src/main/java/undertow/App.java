package undertow;

import java.net.MalformedURLException;
import java.sql.SQLException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;

public class App {
    

    public static final String ROOTPATH = "/";
    public static final String Q2ROUTE = "/q2";
    public static final String Q1ROUTE = "/q1";
    public static final String Q3ROUTE = "/q3";
    public static final String HEALTHCHECKROUTE = "/healthcheck";
    public static void main(String[] args) {
        Undertow server;
        try {
            HttpHandler q1handler=new Q1Handler();
            HttpHandler q2handler=new Q2Handler();
            HttpHandler q3handler=new Q3Handler();
            PathHandler path = Handlers
                     .path(Handlers.redirect(ROOTPATH))
                     .addPrefixPath(Q1ROUTE, q1handler)
                     .addPrefixPath(Q2ROUTE, q2handler)
                     .addPrefixPath(Q3ROUTE, q3handler);
            server = Undertow.builder().setIoThreads(8)
                    .addHttpListener(80, "0.0.0.0").setHandler(path).build();
            
            server.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // try {
        // DeploymentInfo servletBuilder = Servlets.deployment()
        // .setClassLoader(App.class.getClassLoader())
        // .setContextPath(MYAPP)
        // .setDeploymentName("projectCCQ2mysql.war")
        // .addServlet(
        // Servlets.servlet("Q2RequestServlet", Q2Request.class)
        // .addMapping("/q2"))
        // .addServlet(
        // Servlets.servlet("HealthCheckServlet", HealthCheck.class)
        // .addMapping("/HealthCheck"));
        //
        //
        // DeploymentManager manager =
        // Servlets.defaultContainer().addDeployment(servletBuilder);
        // manager.deploy();
        //
        // HttpHandler servletHandler = manager.start();
        // PathHandler path = Handlers
        // .path(Handlers.redirect(MYAPP))
        // .addPrefixPath(MYAPP, servletHandler);
        //
        // Undertow server = Undertow.builder()
        // .addHttpListener(80, "0.0.0.0")
        // .setHandler(path)
        // .build();
        // server.start();
        //
        // } catch (ServletException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }
}
