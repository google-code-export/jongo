package org.jongo;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.sql.Connection;
import org.jongo.jdbc.JDBCConnectionFactory;
import org.jongo.jdbc.JongoJDBCConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Jongo {

    private static final Logger l = LoggerFactory.getLogger(Jongo.class);
    
    public static void main(String[] args) throws Exception{
        l.debug("Starting Jongo");
        
        JongoConfiguration configuration = JongoConfiguration.instanceOf();
        JongoJDBCConnection connection = JDBCConnectionFactory.getConnection();
        Connection conn = connection.getConnection();
        if(conn == null){
            l.error("Failed to load database. Quitting.");
            System.exit(1);
        }
        connection.close();
        
        StringBuilder url = new StringBuilder("http://");
        url.append(configuration.getIp());
        url.append(":");
        url.append(configuration.getPort());
        url.append("/");
        
        
        l.info("Jongo is listening in " + url);
        HttpServer server = HttpServerFactory.create(url.toString());
		server.start();
        
    }
}
