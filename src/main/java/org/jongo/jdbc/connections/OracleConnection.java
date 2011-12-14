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
public class OracleConnection extends AbstractJDBCConnection implements JongoJDBCConnection{
    
    private static final Logger l = LoggerFactory.getLogger(OracleConnection.class);
    
    public OracleConnection(final String url, final String user, final String password) {
        this.url = url;
        this.username = user;
        this.password = password;
        this.driver = JDBCDriver.ORACLE;
    }
    
    @Override
    public void loadDriver() {
        l.debug("Loading Oracle Driver " + this.driver.getName());
        try {
            Class.forName(this.driver.getName());
        } catch (ClassNotFoundException ex) {
            l.error("Unable to load driver. Add the Oracle JDBC Connector jar to the lib folder");
        }
    }

    @Override
    public String getFirstRowQuery(String table) {
        return "SELECT * FROM " + table + " WHERE rownum = 0";
    }
    
    
}
