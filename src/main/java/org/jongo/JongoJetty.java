package org.jongo;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.jongo.admin.JongoAdminServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoJetty{
    
    private static final Logger l = LoggerFactory.getLogger(JongoJetty.class);

    public static void main(String[] args) throws Exception {
        l.info("Starting Jongo in Jetty Embedded mode");
        
        JongoConfiguration configuration = Jongo.loadConfiguration();
        
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "org.jongo.rest");
        
        ServletHolder admin = new ServletHolder(JongoAdminServlet.class);
        
        Server server = new Server(configuration.getPort());
        Context context = new Context(server, "/", Context.SESSIONS);
        context.addServlet(sh, "/jongo/*");
        context.addServlet(admin, "/jongo-admin/*");
        server.start();
        server.join();
    }
}
