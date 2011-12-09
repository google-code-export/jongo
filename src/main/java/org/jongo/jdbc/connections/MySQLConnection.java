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

import org.jongo.enums.JDBCDriver;
import org.jongo.jdbc.AbstractJDBCConnection;
import org.jongo.jdbc.JongoJDBCConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class MySQLConnection extends AbstractJDBCConnection implements JongoJDBCConnection {
    
    private static final Logger l = LoggerFactory.getLogger(MySQLConnection.class);

    public MySQLConnection(final String url, final String user, final String password){
        this.url = url;
        this.username = user;
        this.password = password;
        this.driver = JDBCDriver.MySQL;
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
}
