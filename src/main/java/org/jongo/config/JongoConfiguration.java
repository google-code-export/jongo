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
import org.apache.commons.lang.StringUtils;
import org.jongo.demo.Demo;
import org.jongo.enums.JDBCDriver;
import org.jongo.exceptions.StartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoConfiguration {
    
    private static final Logger l = LoggerFactory.getLogger(JongoConfiguration.class);
    
    private static final String p_name_jongo_ip = "jongo.ip";
    private static final String p_name_jongo_port = "jongo.port";
    private static final String p_name_jongo_allow_apps = "jongo.allow.apps";
    private static final String p_name_jongo_default_limit = "jongo.default.limit";
    private static final String p_name_jongo_max_limit = "jongo.default.max.limit";
    private static final String p_name_jongo_allow_list_tables = "jongo.allow.database.metadata";
    private static final String p_name_jongo_database_list = "jongo.database.list";
    private static final String p_prefix_db_driver = ".jdbc.driver";
    private static final String p_prefix_db_username = ".jdbc.username";
    private static final String p_prefix_db_password = ".jdbc.password";
    private static final String p_prefix_db_url = ".jdbc.url";
    
    private static final String propertiesFileName = "/org/jongo/jongo.properties";
    private static JongoConfiguration instance;
    
    private String ip;
    private int port;
    
    private Integer limit;
    private Integer maxLimit;
    private boolean listTables;
    
    private boolean appsEnabled;
    
    private Map<String, DatabaseConfiguration> databases = null;
    
    private static final boolean demo = (System.getProperty("environment") != null && System.getProperty("environment").equalsIgnoreCase("demo")); 
    
    private JongoConfiguration(){}
    
    public static JongoConfiguration instanceOf(){
        if(instance == null){
            instance = new JongoConfiguration();
            Properties prop = getProperties(instance);
            setProperties(instance, prop);
            
            if(demo){
                l.debug("Loading demo configuration with memory databases");
                instance.databases = Demo.getDemoDatabasesConfiguration();
            }else{
                try {
                    instance.databases = getDatabaseConfigurations(prop);
                } catch (StartupException ex) {
                    l.error(ex.getLocalizedMessage());
                }
            }
            
            if(!instance.isValid()) instance = null;
            
        }
        return instance;
    }
    
    private static void setProperties(JongoConfiguration instance, Properties prop){
        instance.ip = prop.getProperty(p_name_jongo_ip);
        instance.port = Integer.valueOf(prop.getProperty(p_name_jongo_port));
        instance.appsEnabled = Boolean.valueOf(prop.getProperty(p_name_jongo_allow_apps));
        instance.limit = Integer.valueOf(prop.getProperty(p_name_jongo_default_limit));
        instance.maxLimit = Integer.valueOf(prop.getProperty(p_name_jongo_max_limit));
        instance.listTables = Boolean.valueOf(prop.getProperty(p_name_jongo_allow_list_tables));
    }
    
    private static Properties getProperties(JongoConfiguration instance){
        Properties prop;
        if(demo){
            prop = loadDemoProperties();
        }else{
            prop = loadProperties();
        }
        return prop;
    }
    
    private static Properties loadDemoProperties(){
        Properties prop = new Properties();
        prop.setProperty(p_name_jongo_ip, "localhost");
        prop.setProperty(p_name_jongo_port, "8080");
        prop.setProperty(p_name_jongo_allow_apps, "true");
        prop.setProperty(p_name_jongo_default_limit, "25");
        prop.setProperty(p_name_jongo_max_limit, "1000");
        prop.setProperty(p_name_jongo_allow_list_tables, "true");
        return prop;
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
    
    private static Map<String, DatabaseConfiguration> getDatabaseConfigurations(final Properties prop) throws StartupException{
        Map<String, DatabaseConfiguration> databases = new HashMap<String, DatabaseConfiguration>();
        String databaseList = prop.getProperty(p_name_jongo_database_list);
        if(databaseList == null){
            throw new StartupException("Failed to read list of databases " + p_name_jongo_database_list, demo);
        }
        final String [] names = databaseList.split(",");
        for(String name : names){
            name = name.trim();
            if(StringUtils.isAlphanumeric(name)){
                databases.put(name, generateDatabaseConfiguration(prop, name));
            }else{
                l.warn("Database name " + name + " is invalid. Continuing without it.");
            }
        }
        return databases;
    }
    
    private static DatabaseConfiguration generateDatabaseConfiguration(final Properties prop, final String name){
        l.debug("Generating configuration options for database " + name);
        JDBCDriver driver = JDBCDriver.driverOf(prop.getProperty(name + p_prefix_db_driver));
        String username = prop.getProperty(name + p_prefix_db_username);
        String password = prop.getProperty(name + p_prefix_db_password);
        String url = prop.getProperty(name + p_prefix_db_url);
        DatabaseConfiguration c = AbstractDatabaseConfiguration.instanceOf(name, driver, username, password, url);
        return c;
    }
    
    private boolean isValid(){
        boolean ret = true;
        
        if(instance.databases == null){
            ret = false;
        }
        
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
    
    public DatabaseConfiguration getDatabaseConfiguration(final String database){
        return databases.get(database);
    }
    
    public Set<String> getDatabases(){
        return databases.keySet();
    }

    public boolean allowListTables() {
        return listTables;
    }
}
