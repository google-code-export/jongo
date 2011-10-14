package org.jongo;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import org.jongo.jdbc.JDBCConnectionFactory;
import org.jongo.jdbc.JDBCExecutor;
import org.jongo.jdbc.JongoJDBCConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Jongo {

    private static final Logger l = LoggerFactory.getLogger(Jongo.class);
    
    public static void main(String[] args){
        l.info("Starting Jongo in Test Mode");
        
        JongoConfiguration configuration = loadConfiguration();
        
        StringBuilder url = new StringBuilder("http://");
        url.append(configuration.getIp());
        url.append(":");
        url.append(configuration.getPort());
        url.append("/");
        
        
        l.info("Starting Jongo in " + url);
        try {
            HttpServer server = HttpServerFactory.create(url.toString());
            server.start();
        } catch (IOException ex) {
            l.error("Failed to open socket. Quitting");
            l.error(ex.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException ex) {
            l.error("Invalid URL. Fix your configuration. Quitting");
            l.error(ex.getMessage());
            System.exit(1);
        }
    }
    
    public static JongoConfiguration loadConfiguration(){
        JongoConfiguration configuration = null;
        try{
            configuration = JongoConfiguration.instanceOf();
        }catch(IllegalArgumentException e){
            l.error(e.getMessage());
        }
        
        if(configuration == null){
            l.error("Failed to load configuration. Quitting.");
            System.exit(1);
        }
        
        
        JongoJDBCConnection conn = null;
        try{
            conn = JDBCConnectionFactory.getJongoJDBCConnection();
            JDBCExecutor.createJongoTablesAndData();
        }catch(Exception e){
            l.error("Failed to generate Jongo Tables and default configuration.");
            l.error(e.getMessage());
            System.exit(1);
        }
        
        if(conn == null){
            l.error("Failed to load database. Quitting.");
            System.exit(1);
        }
        
        return configuration;
    }
}
