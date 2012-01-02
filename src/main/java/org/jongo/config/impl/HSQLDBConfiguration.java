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
package org.jongo.config.impl;

import org.jongo.config.AbstractDatabaseConfiguration;
import org.jongo.config.DatabaseConfiguration;
import org.jongo.enums.JDBCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class HSQLDBConfiguration extends AbstractDatabaseConfiguration implements DatabaseConfiguration {
    
    private static final Logger l = LoggerFactory.getLogger(HSQLDBConfiguration.class);
    
    private static boolean loaded = false;
    
    public HSQLDBConfiguration(String name, String user, String password, String url){
        this.name = name;
        this.driver = JDBCDriver.HSQLDB;
        this.username = user;
        this.password = password;
        this.url = url;
    }

    @Override
    public void loadDriver() {
        if(!loaded){
            l.debug("Loading HSQLDB Driver " + this.driver.getName());
            try {
                Class.forName(this.driver.getName());
                loaded = true;
            } catch (ClassNotFoundException ex) {
                l.error("Unable to load driver. Add the HSQLDB Connector jar to the lib folder");
            }
        }
    }
    
    /**
     * HSQLDB doesn't support the standard way as described in http://en.wikipedia.org/wiki/Select_(SQL)#FETCH_FIRST_clause
     * @param table
     * @return a HSQLDB query that when executed should only return the first row of a table.
     */
    @Override
    public String getFirstRowQuery(String table) {
        return "SELECT * FROM " + table + " LIMIT 1";
    }
    
}
