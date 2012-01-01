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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jongo.config.DatabaseConfiguration;
import org.jongo.config.JongoConfiguration;
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
    private static final String JONGO_ADMIN = "jongoAdmin";

    private final Map<String, GenericObjectPool> connectionPool = new HashMap<String,GenericObjectPool>();
    
    private static JDBCConnectionFactory instance = null;
    
    private JDBCConnectionFactory(){}
    
    private static JDBCConnectionFactory instanceOf(){
        if(instance == null){
            instance = new JDBCConnectionFactory();
            Set<String> databases = configuration.getDatabases();
            for(String dbname : databases){
                l.debug("Registering Connection Pool for " + dbname);
                DatabaseConfiguration dbcfg = configuration.getDatabaseConfiguration(dbname);
                GenericObjectPool pool = new GenericObjectPool(null, 5);
                ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(dbcfg.getUrl(), dbcfg.getUser(), dbcfg.getPassword());
                PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, pool, null, null, false, true);
                instance.connectionPool.put(dbname, pool);
            }
            
            l.debug("Registering Connection Pool for admin database");
            DatabaseConfiguration dbcfg = configuration.getAdminDatabaseConfiguration();
            GenericObjectPool pool = new GenericObjectPool(null, 5);
            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(dbcfg.getUrl(), dbcfg.getUser(), dbcfg.getPassword());
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, pool, null, null, false, true);
            instance.connectionPool.put(JONGO_ADMIN, pool);
        }
        return instance;
    }
    
    public static JongoJDBCConnection getJongoAdminJDBCConnection() {
        JDBCConnectionFactory me = JDBCConnectionFactory.instanceOf();
        JongoJDBCConnection cx = me.getJongoJDBCConnection(configuration.getAdminDatabaseConfiguration());
        return cx;
    }
    
    public static JongoJDBCConnection getJongoJDBCConnection(final String database){
        JDBCConnectionFactory me = JDBCConnectionFactory.instanceOf();
        return me.getJongoJDBCConnection(configuration.getDatabaseConfiguration(database));
    }

    public JongoJDBCConnection getJongoJDBCConnection(final DatabaseConfiguration conf) {
        JongoJDBCConnection cx = null;
        switch (conf.getDriver()) {
            case MySQL:
                l.debug("New MySQL Connection to " + conf.toString());
                cx = new MySQLConnection(conf);
                break;
            case HSQLDB:
                l.debug("New HSQLDB Connection to " + conf.toString());
                cx = new HSQLConnection(conf);
                break;
            case ORACLE:
                l.debug("New Oracle Connection to " + conf.toString());
                cx = new OracleConnection(conf);
                break;
            default:
                throw new IllegalArgumentException("Not implemented yet");
        }

        return cx;
    }

    public static Connection getConnection(final String database) throws SQLException {
        l.debug("Obtaining a connection from the datasource");
        DataSource ds = getDataSource(database);
        return ds.getConnection();
    }

    public static Connection getAdminConnection() throws SQLException {
        l.debug("Obtaining a connection from the admin datasource");
        DataSource ds = getAdminDataSource();
        return ds.getConnection();
    }

    public static DataSource getDataSource(final String database) {
        JDBCConnectionFactory me = JDBCConnectionFactory.instanceOf();
        PoolingDataSource dataSource = new PoolingDataSource(me.connectionPool.get(database));
        return dataSource;
    }

    public static DataSource getAdminDataSource() {
        JDBCConnectionFactory me = JDBCConnectionFactory.instanceOf();
        PoolingDataSource dataSource = new PoolingDataSource(me.connectionPool.get(JONGO_ADMIN));
        return dataSource;
    }
    
    public static QueryRunner getQueryRunner(final String database){
        DataSource ds = getDataSource(database);
        return new QueryRunner(ds);
    }
    
    public static QueryRunner getAdminQueryRunner(){
        DataSource ds = getDataSource(JONGO_ADMIN);
        return new QueryRunner(ds);
    }
}
