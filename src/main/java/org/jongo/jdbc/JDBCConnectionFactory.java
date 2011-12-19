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

package org.jongo.jdbc;

import org.jongo.jdbc.connections.MySQLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jongo.JongoConfiguration;
import org.jongo.enums.JDBCDriver;
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
    private static final JongoConfiguration configuration = JongoConfiguration.instanceOf();
    private static JongoJDBCConnection connection = null;
    private static JongoJDBCConnection adminConnection = null;
    private static DataSource datasource = null;
    private static DataSource adminDatasource = null;
    
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
                l.debug("New MySQL Connection to " + url);
                cx = new MySQLConnection(url, user, pass);
                break;
            case HSQLDB:
                l.debug("New HSQLDB Connection to " + url);
                cx = new HSQLConnection(url, user, pass);
                break;
            case ORACLE:
                l.debug("New Oracle Connection to " + url);
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

    public static DataSource getDataSource() {
        boolean loadConnection = false;
        if (datasource == null) {
            loadConnection = true;
        }else{
            try {
                loadConnection = datasource.getConnection().isClosed();
            } catch (SQLException ex) {
                l.warn("Failed to check if connection is closed");
                loadConnection = false;
            }
        }
        
        if(loadConnection){
            JongoJDBCConnection conn = getJongoJDBCConnection(configuration.getDriver(), configuration.getJdbcUrl(), configuration.getJdbcUsername(), configuration.getJdbcPassword());
            conn.loadDriver();
            datasource = setupDataSource(conn);
        }
        
        return datasource;
    }

    public static DataSource getAdminDataSource() {
        boolean loadConnection = false;
        if (adminDatasource == null) {
            loadConnection = true;
        }else{
            try {
                loadConnection = adminDatasource.getConnection().isClosed();
            } catch (SQLException ex) {
                l.warn("Failed to check if admin connection is closed");
                loadConnection = false;
            }
        }
        
        if(loadConnection){
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
}
