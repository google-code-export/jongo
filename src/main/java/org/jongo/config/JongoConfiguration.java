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
import org.jongo.JongoShutdown;
import org.jongo.demo.Demo;
import org.jongo.enums.JDBCDriver;
import org.jongo.exceptions.StartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class which loads the jongo.properties files, reads its content and provides methods to access
 * this configuration properties.
 * @author Alejandro Ayuso 
 */
public class JongoConfiguration {
    
    private static final Logger l = LoggerFactory.getLogger(JongoConfiguration.class);
    
    private static final String p_name_jongo_database_list = "jongo.database.list";
    private static final String p_prefix_db_driver = ".jdbc.driver";
    private static final String p_prefix_db_username = ".jdbc.username";
    private static final String p_prefix_db_password = ".jdbc.password";
    private static final String p_prefix_db_database = ".jdbc.database";
    private static final String p_prefix_db_host = ".jdbc.host";
    private static final String p_prefix_db_port = ".jdbc.port";
    private static final String p_prefix_db_readonly = ".jdbc.readonly";
    private static final String p_prefix_db_max_connections = ".jdbc.max.connections";
    
    private static JongoConfiguration instance;
    
    private Integer limit;
    private Integer maxLimit;
    private boolean listTables;
    
    private Map<String, DatabaseConfiguration> databases = null;
    
    private static final boolean demo = (System.getProperty("environment") != null && System.getProperty("environment").equalsIgnoreCase("demo")); 
    
    private JongoConfiguration(){}
    
    /**
     * Loads the configuration file, registers the shutdown hook, calls the generation of 
     * the database configurations and returns and instance of JongoConfiguration.
     * @return an instance of the JongoConfiguration.
     */
    public synchronized static JongoConfiguration instanceOf(){
        if(instance == null){
            instance = new JongoConfiguration();
            Properties prop = getProperties(instance);
            
            l.debug("Registering the shutdown hook");
            Runtime.getRuntime().addShutdownHook(new JongoShutdown());
            
            if(demo){
                l.debug("Loading demo configuration with memory databases");
                instance.databases = Demo.getDemoDatabasesConfiguration();
                Demo.generateDemoDatabases(instance.getDatabases());
            }else{
                l.debug("Loading configuration");
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
    
    private static Properties getProperties(JongoConfiguration conf){
        Properties prop;
        if(demo){
            prop = loadDemoProperties();
        }else{
            prop = loadProperties(conf);
        }
        return prop;
    }
    
    private static Properties loadDemoProperties(){
        return new Properties();
    }
    
    /**
     * Loads the jongo.properties from different locations using different methods.
     * @param conf a JongoConfiguration instance used to obtain a ClassLoader.
     * @return an instance of {@link java.util.Properties} with the properties from the file.
     */
    private static Properties loadProperties(JongoConfiguration conf){
        Properties prop = new Properties();
        InputStream in = JongoConfiguration.class.getClass().getResourceAsStream("/org/jongo/jongo.properties");

        if(in == null){
            l.warn("Couldn't load configuration file /org/jongo/jongo.properties");
            in = JongoConfiguration.class.getClass().getResourceAsStream("/jongo.properties");
        }
        
        if(in == null){
            l.error("Couldn't load configuration file /jongo.properties");
            in = conf.getClass().getClassLoader().getResourceAsStream("jongo.properties");
        }
        
        if(in == null){
            l.error("Couldn't load configuration file jongo.properties quitting");
        }

        try {
            if(in != null){
                prop.load(in);
            }
        } catch (IOException ex) {
            l.error("Failed to load configuration", ex);
        }finally{
            try {
                if(in != null) in.close();
            } catch (IOException ex) {
                l.error(ex.getMessage());
            }
        }
        return prop;
    }
    
    /**
     * From the given properties object, load the the different {@link org.jongo.config.DatabaseConfiguration}.
     * @param prop an instance of {@link java.util.Properties} with the properties from the file.
     * @return a map of {@link org.jongo.config.DatabaseConfiguration} as values, and the name given to the
     * database/schema.
     * @throws StartupException if we're unable to load a {@link org.jongo.config.DatabaseConfiguration}.
     */
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
    
    /**
     * From the given properties object, load a {@link org.jongo.config.DatabaseConfiguration}.
     * @param prop an instance of {@link java.util.Properties} with the properties from the file.
     * @param name the name of the database to load.
     * @return a {@link org.jongo.config.DatabaseConfiguration}for the name given to the
     * database/schema.
     */
    private static DatabaseConfiguration generateDatabaseConfiguration(final Properties prop, final String name){
        l.debug("Generating configuration options for database " + name);
        JDBCDriver driver = JDBCDriver.valueOf(prop.getProperty(name + p_prefix_db_driver));
        String username =   prop.getProperty(name + p_prefix_db_username);
        String password =   prop.getProperty(name + p_prefix_db_password);
        String database =   prop.getProperty(name + p_prefix_db_database);
        String host =       prop.getProperty(name + p_prefix_db_host);
        Integer port =      integerValueOf(prop, name + p_prefix_db_port, driver.getDefaultPort());
        Integer max =       integerValueOf(prop, name + p_prefix_db_max_connections, Integer.valueOf(25));
        Boolean readOnly =  Boolean.valueOf(prop.getProperty(name + p_prefix_db_readonly));
        DatabaseConfiguration c = DatabaseConfiguration.instanceOf(name, driver, username, password, database, host, port, max, readOnly);
        return c;
    }
    
    private static Integer integerValueOf(final Properties prop, final String field, final Integer valueInCaseOfFailure){
        Integer ret;
        try{
            ret = Integer.valueOf(prop.getProperty(field));
        }catch(Exception e){
            ret = valueInCaseOfFailure;
        }
        return ret;
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
