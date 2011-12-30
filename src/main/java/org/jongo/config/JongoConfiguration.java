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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.jongo.demo.Demo;
import org.jongo.enums.JDBCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoConfiguration {
    
    private static final Logger l = LoggerFactory.getLogger(JongoConfiguration.class);
    
    private static final String propertiesFileName = "/org/jongo/jongo.properties";
    private static JongoConfiguration instance;
    
    private String ip;
    private int port;
    
    private Integer limit;
    private Integer maxLimit;
    
    private boolean adminEnabled;
    private String adminIp;
    
    private boolean appsEnabled;
    
    private DatabaseConfiguration admin;
    private Map<String, DatabaseConfiguration> databases = null;
    
    private static final boolean demo = (System.getProperty("environment") != null && System.getProperty("environment").equalsIgnoreCase("demo")); 
    
    private JongoConfiguration(){}
    
    public static JongoConfiguration instanceOf(){
        if(instance == null){
            instance = new JongoConfiguration();
            Properties prop = loadProperties();
            instance.ip = prop.getProperty("jongo.ip");
            instance.port = Integer.valueOf(prop.getProperty("jongo.port"));
            instance.adminIp = prop.getProperty("jongo.admin.ip");
            instance.adminEnabled = Boolean.valueOf(prop.getProperty("jongo.admin.enabled"));
            instance.appsEnabled = Boolean.valueOf(prop.getProperty("jongo.allow.apps"));
            instance.limit = Integer.valueOf(prop.getProperty("jongo.default.limit"));
            instance.maxLimit = Integer.valueOf(prop.getProperty("jongo.default.max.limit"));
            
            if(demo){
                l.debug("Loading demo configuration with memory databases");
                instance.admin = new DatabaseConfiguration("jongoAdmin", JDBCDriver.HSQLDB, "jongoAdmin", "jongoAdmin", "jdbc:hsqldb:mem:adminDemo");
                instance.databases = Demo.getDemoDatabasesConfiguration();
            }else{
                instance.databases = getDatabaseConfigurations(prop);
                instance.admin = new DatabaseConfiguration("jongoAdmin", JDBCDriver.HSQLDB, "jongoAdmin", "jongoAdmin", "jdbc:hsqldb:file:data/jongoAdmin");
            }
            
            if(!instance.isValid()) instance = null;
            
        }
        return instance;
    }
    
    private static Properties loadProperties(){
        Properties prop = new Properties();
        InputStream in = JongoConfiguration.class.getClass().getResourceAsStream(propertiesFileName);

        if(in == null){
            l.warn("Couldn't load configuration file " + propertiesFileName);
            in = JongoConfiguration.class.getClass().getResourceAsStream("/jongo.properties");
            if(in == null){
                l.error("Couldn't load configuration file /jongo.properties quitting");
                System.exit(1);
            }
        }

        try {
            if(in != null){
                prop.load(in);
            }
        } catch (IOException ex) {
            l.error("Failed to load " + propertiesFileName, ex);
            System.exit(1);
        }finally{
            try {
                in.close();
            } catch (IOException ex) {
                l.error(ex.getMessage());
            }
        }
        return prop;
    }
    
    private static Map<String, DatabaseConfiguration> getDatabaseConfigurations(final Properties prop){
        Map<String, DatabaseConfiguration> databases = new HashMap<String, DatabaseConfiguration>();
        final String [] names = prop.getProperty("jongo.databases").split(",");
        return databases;
    }
    
    private boolean isValid(){
        boolean ret = true;
        return ret;
    }

    public JDBCDriver getDriver(final String database) {
        DatabaseConfiguration db = this.databases.get(database);
        return db.getDriver();
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getAdminIp() {
        return adminIp;
    }

    public boolean isAdminEnabled() {
        return adminEnabled;
    }

    public boolean areAppsEnabled() {
        return appsEnabled;
    }
    
    public boolean isDemoModeActive(){
        return demo;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }
    
    public DatabaseConfiguration getAdminDatabaseConfiguration(){
        return admin;
    }
    
    public DatabaseConfiguration getDatabaseConfiguration(final String database){
        return databases.get(database);
    }
    
    public Set<String> getDatabases(){
        return databases.keySet();
    }
}
