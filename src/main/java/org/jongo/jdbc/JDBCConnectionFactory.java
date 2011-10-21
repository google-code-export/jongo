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
import org.jongo.enums.JDBCDriver;
import org.jongo.jdbc.connections.HSQLConnection;
import org.jongo.jdbc.connections.OracleConnection;
import org.jongo.jdbc.exceptions.HSQLException;
import org.jongo.jdbc.exceptions.JongoJDBCException;
import org.jongo.jdbc.exceptions.MySQLException;
import org.jongo.jdbc.exceptions.OracleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JDBCConnectionFactory {

    private static final Logger l = LoggerFactory.getLogger(JDBCConnectionFactory.class);
    private static final JongoConfiguration configuration = JongoConfiguration.instanceOf();
    private static JongoJDBCConnection connection = null;
    private static JongoJDBCConnection adminConnection = null;
    private static DataSource datasource = null;
    private static DataSource adminDatasource = null;
    
    public static final String JONGOTABLE = "JongoTable";
    public static final String JONGOQUERY = "JongoQuery";

    public static JongoJDBCConnection getJongoJDBCConnection() {
        if(connection == null){
            connection = getJongoJDBCConnection(configuration.getDriver(), configuration.getJdbcUrl(), configuration.getJdbcUsername(), configuration.getJdbcPassword());
        }
        return connection;
    }

    public static JongoJDBCConnection getJongoAdminJDBCConnection() {
        if(adminConnection == null){
            adminConnection = getJongoJDBCConnection(configuration.getAdminDriver(), configuration.getJdbcAdminUrl(), configuration.getJdbcAdminUsername(), configuration.getJdbcAdminPassword());
        }
        return adminConnection;
    }

    public static JongoJDBCConnection getJongoJDBCConnection(JDBCDriver driver, String url, String user, String pass) {
        JongoJDBCConnection cx = null;
        switch (driver) {
            case MySQL:
                cx = new MySQLConnection(url, user, pass);
                break;
            case HSQLDB:
                l.debug("New HSQLDB Connection to " + url);
                cx = new HSQLConnection(url, user, pass);
                break;
            case ORACLE:
                cx = new OracleConnection(url, user, pass);
                break;
            default:
                throw new IllegalArgumentException("Not implemented yet");
        }

        return cx;
    }

    public static Connection getConnection() throws SQLException {
        l.debug("Obtaining a connection from the datasource");
        DataSource ds = getDataSource();
        return ds.getConnection();
    }

    public static Connection getAdminConnection() throws SQLException {
        l.debug("Obtaining a connection from the admin datasource");
        DataSource ds = getAdminDataSource();
        return ds.getConnection();
    }

    public static DataSource getDataSourceForTable(final String table) {
        if (JONGOTABLE.equalsIgnoreCase(table) || JONGOQUERY.equalsIgnoreCase(table)) {
            return getAdminDataSource();
        } else {
            return getDataSource();
        }
    }

    private static DataSource getDataSource() {
        if (datasource == null) {
            JongoJDBCConnection conn = getJongoJDBCConnection(configuration.getDriver(), configuration.getJdbcUrl(), configuration.getJdbcUsername(), configuration.getJdbcPassword());
            conn.loadDriver();
            datasource = setupDataSource(conn);
        }
        return datasource;
    }

    private static DataSource getAdminDataSource() {
        if (adminDatasource == null) {
            JongoJDBCConnection conn = getJongoJDBCConnection(configuration.getAdminDriver(), configuration.getJdbcAdminUrl(), configuration.getJdbcAdminUsername(), configuration.getJdbcAdminPassword());
            conn.loadDriver();
            adminDatasource = setupDataSource(conn);
        }
        return adminDatasource;
    }

    public static DataSource setupDataSource(final JongoJDBCConnection conn) {
        l.debug("Setting up Pooling Data Source");
        GenericObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(conn.getUrl(), conn.getUsername(), conn.getPassword());
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
        return dataSource;
    }

    public static JongoJDBCException getException(final String msg, final SQLException e) {
        switch (configuration.getDriver()) {
            case MySQL:
                return new MySQLException(msg, e);
            case HSQLDB:
                return new HSQLException(msg, e);
            case ORACLE:
                return new OracleException(msg, e);
            default:
                throw new IllegalArgumentException("Not implemented yet");
        }
    }

    public static JongoJDBCException getException(final String msg, final int e) {
        switch (configuration.getDriver()) {
            case MySQL:
                return new MySQLException(msg, e);
            case HSQLDB:
                return new HSQLException(msg, e);
            case ORACLE:
                return new OracleException(msg, e);
            default:
                throw new IllegalArgumentException("Not implemented yet");
        }
    }
}
