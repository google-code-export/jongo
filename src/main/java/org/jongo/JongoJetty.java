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

import org.jongo.config.JongoConfiguration;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.ArrayList;
import java.util.List;
import org.jongo.demo.Demo;
import org.jongo.exceptions.StartupException;
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
        
        l.debug("Loading Configuration");
        JongoConfiguration configuration = null;
        try {
            configuration = JongoUtils.loadConfiguration();
        } catch (StartupException ex) {
            l.error(ex.getMessage());
            if(ex.isFatal()){
                System.exit(1);
            }
        }
        
        l.debug("Loading Admin Database");
        try {
            JongoUtils.loadAdminDatabase(configuration);
        } catch (StartupException ex) {
            l.error(ex.getMessage());
            if(ex.isFatal()){
                System.exit(1);
            }
        }
        
        if(configuration.isDemoModeActive()){
            Demo.generateDemoDatabases(configuration.getDatabases());
        }
        
        l.debug("Creating Contexts for Jetty");
        List<Context> contextsList = new ArrayList<Context>();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        Context mainContext = new Context(contexts, "/", Context.NO_SESSIONS);
        
        addDatabaseServletsToMainContext(configuration, mainContext);
        
        if(configuration.isAdminEnabled()){
            addAdminConsoleServletToContexts(contexts, contextsList);
            addAdminWebservicesToMainContext(mainContext);
        }
        
        if(configuration.areAppsEnabled()){
            addApplicationsServletsToContexts(contexts, contextsList);
        }
        
        contextsList.add(mainContext);
        
        l.debug("Registering contexts");
        contexts.setHandlers(contextsList.toArray(new Handler[]{}));
        
        l.debug("Starting Jetty");
        Server server = new Server(configuration.getPort());
        server.setHandler(contexts);
        server.start();
        server.join();
    }
    
    /**
     * Read the database names from the configuration, generate a the servlets and add them to
     * the main context
     * @param conf JongoConfiguration to read the database names
     * @param mainContext the context to add the new servlets
     */
    private static void addDatabaseServletsToMainContext(final JongoConfiguration conf, final Context mainContext){
        l.debug("Creating Servlet for Jongo Webservices under org.jongo.rest");
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "org.jongo.rest");
        for(final String database : conf.getDatabases()){
            final String servlet = "/" + database + "/*";
            l.debug("Adding new servlet to main context " + database + " at " + servlet);
            mainContext.addServlet(sh, servlet);
        }
    }
    
    /**
     * Register the administration console servlet context in the contexts list which are then registered.
     * @param contexts the contexts where the new context is registered against.
     * @param contextsList the holder list for the contexts.
     */
    private static void addAdminConsoleServletToContexts(final ContextHandlerCollection contexts, final List<Context> contextsList){
        l.info("Admin Console is enabled. Creating its context");
        Context ctxADocs= new Context(contexts, "/admin", Context.NO_SESSIONS);
        ctxADocs.setResourceBase("admin/"); // here we set where the servlet will look for the files
        l.debug("Creating Servlet for Admin Console");
        ServletHandler staticHandler = new ServletHandler();
        ServletHolder staticHolder = new ServletHolder( new DefaultServlet() );
        staticHolder.setInitParameter("dirAllowed", "false");
        staticHolder.setServlet(new DefaultServlet());
        staticHandler.addServletWithMapping( staticHolder, "/*" );
        ctxADocs.addServlet(staticHolder, "/");
        contextsList.add(ctxADocs);
    }
    
    /**
     * Register the administration console webservice in the main context.
     * @param mainContext the context where to regiter the servlet.
     */
    private static void addAdminWebservicesToMainContext(final Context mainContext){
        l.debug("Creating Servlet for Jongo Admin Webservices on /adminws/");
        ServletHolder shAdmin = new ServletHolder(ServletContainer.class);
        shAdmin.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        shAdmin.setInitParameter("com.sun.jersey.config.property.packages", "org.jongo.admin");
        mainContext.addServlet(shAdmin, "/adminws/*");
    }
    
    /**
     * Reads the list of applications from the apps folder, register a new context and servlet for each
     * and add them to the list of contexts
     * @param contexts the contexts where the new context is registered against.
     * @param contextsList the holder list for the contexts.
     */
    private static void addApplicationsServletsToContexts(final ContextHandlerCollection contexts, final List<Context> contextsList){
        l.debug("Loading apps");
        List<String> apps = JongoUtils.getListOfApps();
        for(String app : apps){
            l.debug("Creating Servlet for " + app);
            Context ctxADocs= new Context(contexts, "/" + app ,Context.NO_SESSIONS);
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
}
