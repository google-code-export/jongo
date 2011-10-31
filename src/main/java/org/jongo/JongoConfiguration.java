package org.jongo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
    
    private JDBCDriver driver;
    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcPassword;
    
    private boolean adminEnabled = false;
    private String adminIp;
    
    private static final JDBCDriver adminDriver = JDBCDriver.HSQLDB;
    private static final String jdbcAdminUrl = "jdbc:hsqldb:file:data/jongoAdmin";
    private static final String jdbcAdminUsername = "jongoAdmin";
    private static final String jdbcAdminPassword = "jongoAdmin";
    
    private JongoConfiguration(){}
    
    public static JongoConfiguration instanceOf(){
        if(instance == null){
            instance = new JongoConfiguration();
            Properties prop = loadProperties();
            instance.ip = prop.getProperty("jongo.ip");
            instance.port = Integer.valueOf(prop.getProperty("jongo.port"));
            instance.driver = JDBCDriver.driverOf(prop.getProperty("jongo.jdbc.driver"));
            instance.jdbcUrl = prop.getProperty("jongo.jdbc.url");
            instance.jdbcUsername = prop.getProperty("jongo.jdbc.username");
            instance.jdbcPassword = prop.getProperty("jongo.jdbc.password");
            instance.adminIp = prop.getProperty("jongo.admin.ip");
            instance.adminEnabled = Boolean.getBoolean(prop.getProperty("jongo.admin.enabled"));
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

    public JDBCDriver getDriver() {
        return driver;
    }

    public String getIp() {
        return ip;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public int getPort() {
        return port;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    public String getAdminIp() {
        return adminIp;
    }

    public JDBCDriver getAdminDriver() {
        return adminDriver;
    }

    public String getJdbcAdminPassword() {
        return jdbcAdminPassword;
    }

    public String getJdbcAdminUrl() {
        return jdbcAdminUrl;
    }

    public String getJdbcAdminUsername() {
        return jdbcAdminUsername;
    }

    public boolean isAdminEnabled() {
        return adminEnabled;
    }
}
