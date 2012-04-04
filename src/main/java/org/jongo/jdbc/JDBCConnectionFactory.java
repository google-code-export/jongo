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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JDBCConnectionFactory {

    private static final Logger l = LoggerFactory.getLogger(JDBCConnectionFactory.class);
    private static final JongoConfiguration configuration = JongoConfiguration.instanceOf();

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
                GenericObjectPool pool = new GenericObjectPool(null, 25);
                ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(dbcfg.getUrl(), dbcfg.getUsername(), dbcfg.getPassword());
                PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, pool, null, null, false, true);
                poolableConnectionFactory.hashCode();
                instance.connectionPool.put(dbname, pool);
            }
        }
        return instance;
    }

    public static Connection getConnection(final String database) throws SQLException {
        l.debug("Obtaining a connection from the datasource");
        DataSource ds = getDataSource(database);
        return ds.getConnection();
    }

    public static DataSource getDataSource(final String database) {
        JDBCConnectionFactory me = JDBCConnectionFactory.instanceOf();
        PoolingDataSource dataSource = new PoolingDataSource(me.connectionPool.get(database));
        return dataSource;
    }

    public static QueryRunner getQueryRunner(final String database){
        DataSource ds = getDataSource(database);
        return new QueryRunner(ds);
    }
    
    public static void closeConnections() throws SQLException{
        for(String dbname : configuration.getDatabases()){
            l.debug("Shutting down JDBC connection " + dbname);
            getConnection(dbname).close();
        }
    }
}
