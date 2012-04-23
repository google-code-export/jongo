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
     * Name of the configuration. It should be unique for the whole jongo instance. This is the name
     * that will be used to access this configuration.
     */
    protected final String name;
    
    /**
     * the {@link org.jongo.enums.JDBCDriver} driver.
     */
    protected final JDBCDriver driver;
    
    /**
     * the username used to authenticate against the RDMBS.
     */
    protected final String username;
    
    /**
     * the password for the given user used to authenticate against the RDMBS.
     */
    protected final String password;
    
    /**
     * The name of the database to connect to.
     */
    protected final String database;
    
    /**
     * Host of the RBDMS.
     */
    protected final String host;
    
    /**
     * The port where the RDBMS is listening.
     */
    protected final Integer port;
    
    /**
     * Set the connection to read-only.
     */
    protected final boolean readOnly;

    private boolean loaded = false;

    private DatabaseConfiguration(String name, JDBCDriver driver, String username, String password, String database, String host, Integer port, boolean readOnly) {
        this.name = name;
        this.driver = driver;
        this.username = username;
        this.password = password;
        this.database = database;
        this.host = host;
        this.port = port;
        this.readOnly = readOnly;
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
    public static DatabaseConfiguration instanceOf(String name, JDBCDriver driver, String username, String password, String database, String host, Integer port, boolean readOnly){
        DatabaseConfiguration c = new DatabaseConfiguration(name, driver, username, password, database, host, port, readOnly);
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

    public String getUsername() {
        return username;
    }

    public String getDatabase() {
        return database;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
    
    public String toJdbcURL(){
        final StringBuilder b = new StringBuilder("jdbc:");
        switch(driver){
            case HSQLDB_MEM:
                b.append("hsqldb:mem:").append(database);
                break;
            case HSQLDB_FILE:
                b.append("hsqldb:file:").append(database);
                break;
            case MSSQL:
                b.append("microsoft:sqlserver://").append(host).append(":").append(port).append(";databasename=").append(database);
                break;
            case MSSQL_JTDS:
                b.append("jtds:sqlserver://").append(host).append(":").append(port).append("/").append(database);
                break;
            case MySQL:
                b.append("mysql://").append(host).append(":").append(port).append("/").append(database);
                break;
            case ORACLE:
                b.append("oracle:thin:@//").append(host).append(":").append(port).append("/").append(database);
                break;
            case PostgreSQL:
                b.append("postgresql://").append(host).append(":").append(port).append("/").append(database);
                break;
        }
        return b.toString();
    }

    @Override
    public String toString() {
        return "DatabaseConfiguration{" + "name=" + name + ", driver=" 
                + driver + ", username=" + username + ", password=" 
                + password + ", database=" + database + ", host=" 
                + host + ", port=" + port + ", readOnly=" + readOnly + '}';
    }
}
