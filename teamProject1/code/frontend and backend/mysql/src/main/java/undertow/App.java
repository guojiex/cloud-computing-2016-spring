package undertow;

import java.net.MalformedURLException;
import java.sql.SQLException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;

public class App {
    

    
    public static final String MYAPP = "/";
    public static void main(String[] args) {
        //HttpHandler myhandler=new MyHandler();
        Undertow server;
        try {
            HttpHandler handler=new MyHandler();
            PathHandler path = Handlers
                     .path(Handlers.redirect(MYAPP))
                     .addPrefixPath(MYAPP, handler);
            server = Undertow.builder().setIoThreads(64)
                    .addHttpListener(80, "0.0.0.0").setHandler(path).build();
            
            server.start();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
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
