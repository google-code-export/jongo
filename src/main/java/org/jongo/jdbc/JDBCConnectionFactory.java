package org.jongo.jdbc;

import org.jongo.jdbc.connections.MySQLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jongo.JongoConfiguration;
import org.jongo.jdbc.connections.HSQLConnection;
import org.jongo.jdbc.connections.OracleConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JDBCConnectionFactory {
    
    private static final Logger l = LoggerFactory.getLogger(JDBCConnectionFactory.class);
    private static JongoJDBCConnection connection = null;
    private static DataSource datasource = null;
    
    public static JongoJDBCConnection getJongoJDBCConnection(){
        if(connection == null){
            JongoConfiguration configuration = JongoConfiguration.instanceOf();
            switch(configuration.getDriver()){
                case MySQL:
                    connection = new MySQLConnection(configuration.getJdbcUrl(), configuration.getJdbcUsername(), configuration.getJdbcPassword());
                    break;
                case HSQLDB:
                    connection = new HSQLConnection(configuration.getJdbcUrl(), configuration.getJdbcUsername(), configuration.getJdbcPassword());
                    break;
                case ORACLE:
                    connection = new OracleConnection(configuration.getJdbcUrl(), configuration.getJdbcUsername(), configuration.getJdbcPassword());
                default:
                    throw new IllegalArgumentException("Not implemented yet");
            }
        }
        
        return connection;
    }
    
    public static Connection getConnection() throws SQLException{
        l.debug("Obtaining a connection from the datasource");
        DataSource ds = getDataSource();
        return ds.getConnection();
    }
    
    public static DataSource getDataSource(){
        if(datasource == null){
            JongoConfiguration configuration = JongoConfiguration.instanceOf();
            JongoJDBCConnection conn = getJongoJDBCConnection();
            conn.loadDriver();
            setupDataSource(configuration.getJdbcUrl(), configuration.getJdbcUsername(), configuration.getJdbcPassword());
        }
        return datasource;
    }
    
    public static void setupDataSource(final String url, final String username, final String password){
        l.debug("Setting up Pooling Data Source");
        ObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, username, password);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
        datasource = dataSource;
    }
    
}
