package org.jongo;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHandler;
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
        
        l.debug("Creating Contexts for Jetty");
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        Context context = new Context(contexts, "/", Context.SESSIONS);
        
        
        
        l.debug("Creating Servlet for Jongo Webservices under org.jongo.rest");
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "org.jongo.rest");
        context.addServlet(sh, "/jongo/*");
        
        if(configuration.isAdminEnabled()){
            l.info("Admin Console is enabled. Creating its context");
            Context ctxADocs= new Context(contexts,"/admin",Context.SESSIONS);
            ctxADocs.setResourceBase("admin/"); // here we set where the servlet will look for the files
            l.debug("Creating Servlet for Admin Console");
            ServletHandler staticHandler = new ServletHandler();
            ServletHolder staticHolder = new ServletHolder( new DefaultServlet() );
            staticHolder.setInitParameter("dirAllowed", "false");
            staticHolder.setServlet(new DefaultServlet());
            staticHandler.addServletWithMapping( staticHolder, "/*" );
            ctxADocs.addServlet(staticHolder, "/");
            
            l.debug("Creating Servlet for Jongo Admin Webservices under org.jongo.admin");
            ServletHolder shAdmin = new ServletHolder(ServletContainer.class);
            shAdmin.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
            shAdmin.setInitParameter("com.sun.jersey.config.property.packages", "org.jongo.admin");
            context.addServlet(shAdmin, "/adminws/*");
            
            l.debug("Registering contexts");
            contexts.setHandlers(new Handler[] { ctxADocs, context });
        }else{
            l.debug("Registering contexts");
            contexts.setHandlers(new Handler[] { context });
        }
        
        
        l.debug("Starting Jetty with the new contexts");
        Server server = new Server(configuration.getPort());
        server.setHandler(contexts);
        server.start();
        server.join();
    }
}
