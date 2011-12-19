/**
 * Copyright (C) 2011, 2012 Alejandro Ayuso
 *
 * This file is part of Jongo.
 * Jongo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * Jongo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jongo.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jongo;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.ArrayList;
import java.util.List;
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
        
        l.debug("Registering the shutdown hook");
        Runtime.getRuntime().addShutdownHook(new JongoShutdown());
        
        JongoConfiguration configuration = Jongo.loadConfiguration();
        
        l.debug("Creating Contexts for Jetty");
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        Context mainContext = new Context(contexts, "/", Context.SESSIONS);
        
        l.debug("Creating Servlet for Jongo Webservices under org.jongo.rest");
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "org.jongo.rest");
        mainContext.addServlet(sh, configuration.getJongoServletAddress());
        
        List<Context> contextsList = new ArrayList<Context>();
        
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
            contextsList.add(ctxADocs);
            
            l.debug("Creating Servlet for Jongo Admin Webservices under org.jongo.admin");
            ServletHolder shAdmin = new ServletHolder(ServletContainer.class);
            shAdmin.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
            shAdmin.setInitParameter("com.sun.jersey.config.property.packages", "org.jongo.admin");
            mainContext.addServlet(shAdmin, "/adminws/*");
        }
        
        if(configuration.areAppsEnabled()){
            l.debug("Loading apps");
            List<String> apps = JongoUtils.getListOfApps();
            for(String app : apps){
                l.debug("Creating Servlet for " + app);
                Context ctxADocs= new Context(contexts, "/" + app ,Context.SESSIONS);
                ctxADocs.setResourceBase("apps/" + app + "/"); // here we set where the servlet will look for the files
                ServletHandler staticHandler = new ServletHandler();
                ServletHolder staticHolder = new ServletHolder( new DefaultServlet() );
                staticHolder.setInitParameter("dirAllowed", "false");
                staticHolder.setServlet(new DefaultServlet());
                staticHandler.addServletWithMapping( staticHolder, "/*" );
                ctxADocs.addServlet(staticHolder, "/");
                contextsList.add(ctxADocs);
            }
        }
        
        contextsList.add(mainContext);
        
        l.debug("Registering contexts");
        contexts.setHandlers(contextsList.toArray(new Handler[]{}));
        
        l.debug("Starting Jetty with the new contexts");
        Server server = new Server(configuration.getPort());
        server.setHandler(contexts);
        server.start();
        server.join();
    }
}
