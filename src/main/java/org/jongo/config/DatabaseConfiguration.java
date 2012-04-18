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
 * @author Alejandro Ayuso 
 */
public class DatabaseConfiguration {
    
    private static final Logger l = LoggerFactory.getLogger(DatabaseConfiguration.class);
    
    /**
     * name of the database/schema. It should be unique for the whole jongo instance.
     */
    protected final String name;
    
    /**
     * the {@link org.jongo.enums.JDBCDriver} driver
     */
    protected final JDBCDriver driver;
    
    /**
     * the username used to authenticate against the RDMBS
     */
    protected final String username;
    
    /**
     * the password for the given user used to authenticate against the RDMBS
     */
    protected final String password;
    
    /**
     * the JDBC url of the RDMBS
     */
    protected final String url;

    private boolean loaded = false;

    public DatabaseConfiguration(String name, JDBCDriver driver, String username, String password, String url) {
        this.name = name;
        this.driver = driver;
        this.username = username;
        this.password = password;
        this.url = url;
    }
    
    /**
     * Instantiates a new DatabaseConfiguration object, loads the given JDBCDriver and returns the instance.
     * @param name name of the database/schema. It should be unique for the whole jongo instance.
     * @param driver the {@link org.jongo.enums.JDBCDriver} driver
     * @param user the user used used to authenticate against the RDMBS
     * @param password the password for the given user used to authenticate against the RDMBS
     * @param url the JDBC url of the RDMBS
     * @return an instance of DatabaseConfiguration.
     */
    public static DatabaseConfiguration instanceOf(String name, JDBCDriver driver, String user, String password, String url){
        DatabaseConfiguration c = new DatabaseConfiguration(name, driver, user, password, url);
        c.loadDriver();
        return c;
    }
    
    /**
     * Loads the driver given using Class.forName
     */
    private void loadDriver() {
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
