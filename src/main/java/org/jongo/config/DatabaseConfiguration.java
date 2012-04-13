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
package org.jongo.config;

import org.jongo.enums.JDBCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Holder for database configuration data
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class DatabaseConfiguration {
    
    private static final Logger l = LoggerFactory.getLogger(DatabaseConfiguration.class);
    
    protected final String name;
    protected final JDBCDriver driver;
    protected final String username;
    protected final String password;
    protected final String url;

    private boolean loaded = false;

    public DatabaseConfiguration(String name, JDBCDriver driver, String username, String password, String url) {
        this.name = name;
        this.driver = driver;
        this.username = username;
        this.password = password;
        this.url = url;
    }
    
    public static DatabaseConfiguration instanceOf(String name, JDBCDriver driver, String user, String password, String url){
        DatabaseConfiguration c = new DatabaseConfiguration(name, driver, user, password, url);
        c.loadDriver();
        return c;
    }
    
    public void loadDriver() {
        if(!loaded){
            l.debug("Loading Driver " + this.driver.getName());
            try {
                Class.forName(this.driver.getName());
                loaded = true;
            } catch (ClassNotFoundException ex) {
                l.error("Unable to load driver. Add the JDBC Connector jar to the lib folder");
            }
        }
    }
    
    public JDBCDriver getDriver() {
        return driver;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }
    
    @Override
    public String toString() {
        return "DatabaseConfiguration{" + "name=" + name + ", driver=" + driver + ", user=" + username + ", password=" + password + ", url=" + url + '}';
    }
    
    
}
