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

import org.apache.commons.lang.StringUtils;
import org.jongo.config.AbstractDatabaseConfiguration;
import org.jongo.config.DatabaseConfiguration;
import org.jongo.enums.JDBCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PostgreSQL DatabaseConfiguration implementation. Needs to be tested. Volunteers?
 */
public class PostgreSQLConfiguration extends AbstractDatabaseConfiguration implements DatabaseConfiguration {

    private static final Logger l = LoggerFactory.getLogger(PostgreSQLConfiguration.class);
    
    private static boolean loaded = false;
    
    public PostgreSQLConfiguration(String name, String user, String password, String url){
        this.name = name;
        this.driver = JDBCDriver.PostgreSQL;
        this.username = user;
        this.password = password;
        this.url = url;
    }
    
    @Override
    public void loadDriver() {
        if(!loaded){
            l.debug("Loading PostgreSQL Driver " + this.driver.getName());
            try {
                Class.forName(this.driver.getName());
                loaded = true;
            } catch (ClassNotFoundException ex) {
                l.error("Unable to load driver. Add the PostgreSQL Connector jar to the lib folder");
            }
        }
    }

    @Override
    public String getListOfTablesQuery() {
        return "SELECT * FROM information_schema.tables WHERE table_schema = 'public'";
    }
    
    @Override
    public boolean isValid() {
        if(super.isValid()){
            if(!StringUtils.startsWith(url, " jdbc:postgresql:")){
                l.warn("Invalid JDBC URL. Check your configuration.");
                return false;
            }
            return true;
        }else{
            return false;
        }
    }
    
}
