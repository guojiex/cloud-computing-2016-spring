package server;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import javax.servlet.ServletException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import servlet.HealthCheck;
import servlet.Q2Request;

/**
 * @author wyen Wei-Yu Yen.
 */
public class UndertowServer {

    /**
     * @param args
     * @throws UnknownHostException
     * @throws ServletException
     */
    public static void main(String[] args) throws UnknownHostException, ServletException {
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(UndertowServer.class.getClassLoader())
                .setContextPath("/*")
                .setDeploymentName("q1.war")
                .addServlets(
                        Servlets.servlet("HealthCheck", HealthCheck.class)
                                .addMapping("/HealthCheck"),
                        Servlets.servlet("Q2Request", Q2Request.class).addMapping("/q2"));

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        PathHandler path = Handlers.path(Handlers.redirect("/"))
                .addPrefixPath("/", manager.start());

        Undertow server = Undertow.builder()
                .addHttpListener(80, Inet4Address.getLocalHost().getHostAddress())

        .setHandler(path).build();
        server.start();
    }

}
