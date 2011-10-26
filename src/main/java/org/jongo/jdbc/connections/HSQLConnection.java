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
public class HSQLConnection extends AbstractJDBCConnection implements JongoJDBCConnection{

    private static final Logger l = LoggerFactory.getLogger(HSQLConnection.class);
    
    public HSQLConnection(final String url, final String user, final String password) {
        this.url = url;
        this.username = user;
        this.password = password;
        this.driver = JDBCDriver.HSQLDB;
    }

    @Override
    public void loadDriver() {
        l.debug("Loading HSQLDB Driver " + this.driver.getName());
        try {
            Class.forName(this.driver.getName());
        } catch (ClassNotFoundException ex) {
            l.error("Unable to load driver. Add the HSQLDB Connector jar to the lib folder");
        }
    }
}
