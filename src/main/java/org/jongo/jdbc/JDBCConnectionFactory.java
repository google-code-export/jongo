package org.jongo.jdbc;

import org.jongo.JongoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JDBCConnectionFactory {
    
    private static final Logger l = LoggerFactory.getLogger(JDBCConnectionFactory.class);
    
    public static JongoJDBCConnection getConnection(){
        JongoConfiguration configuration = JongoConfiguration.instanceOf();
        
        JongoJDBCConnection conn = null;
        
        switch(configuration.getDriver()){
            case MySQL:
                conn = new MySQLConnection(configuration.getJdbcUrl(), configuration.getJdbcUsername(), configuration.getJdbcPassword());
                break;
            default:
                throw new IllegalArgumentException("Not implemented yet");
        }
        
        return conn;
    }
    
}
