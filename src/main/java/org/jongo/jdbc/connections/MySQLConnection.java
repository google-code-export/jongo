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

package org.jongo.jdbc.connections;

import org.jongo.config.DatabaseConfiguration;
import org.jongo.jdbc.AbstractJDBCConnection;
import org.jongo.jdbc.JongoJDBCConnection;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class MySQLConnection extends AbstractJDBCConnection implements JongoJDBCConnection {
    
    private static final Logger l = LoggerFactory.getLogger(MySQLConnection.class);

    public MySQLConnection(final DatabaseConfiguration conf){
        this.url = conf.getUrl();
        this.username = conf.getUser();
        this.password = conf.getPassword();
        this.driver = conf.getDriver();
    }

    @Override
    public void loadDriver() {
        l.debug("Loading MySQL Driver " + this.driver.getName());
        try {
            Class.forName(this.driver.getName());
        } catch (ClassNotFoundException ex) {
            l.error("Unable to load driver. Add the MySQL Connector jar to the lib folder");
        }
    }
    
    /**
     * MySQL doesn't support the standard way as described in http://en.wikipedia.org/wiki/Select_(SQL)#FETCH_FIRST_clause
     * @param table
     * @return a MySQL query that when executed should only return the first row of a table.
     */
    @Override
    public String getFirstRowQuery(String table) {
        return "SELECT * FROM " + table + " LIMIT 1";
    }
    
    @Override
    public String getSelectAllFromTableQuery(final String table, LimitParam limit, OrderParam order){
        final StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(table);
        if(order != null){
            query.append(" ORDER BY ");
            query.append(order.getColumn());
            query.append(" ");
            query.append(order.getDirection());
        }
        if(limit != null){
            query.append(" LIMIT ");
            query.append(limit.getLimit());
            query.append(" OFFSET ");
            query.append(limit.getStart());
        }
        
        return query.toString();
    }
}
