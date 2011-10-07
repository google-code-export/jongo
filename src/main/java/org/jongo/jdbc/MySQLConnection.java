package org.jongo.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.jongo.enums.JDBCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class MySQLConnection extends AbstractJDBCConnection implements JongoJDBCConnection {
    
    private static final Logger l = LoggerFactory.getLogger(MySQLConnection.class);

    public MySQLConnection(final String url, final String user, final String password){
        this.url = url;
        this.username = user;
        this.password = password;
        this.driver = JDBCDriver.MySQL;
    }

    @Override
    public Connection getConnection() {
        l.debug("Loading MySQL Driver " + this.driver.getName());
            try {
                Class.forName(this.driver.getName());
                this.conn = DriverManager.getConnection(url, username, password);
            } catch (ClassNotFoundException ex) {
                l.error("Unable to load driver. Add the MySQL Connector jar to the lib folder");
            } catch (SQLException ex) {
                l.error(ex.getMessage());
            }
        return this.conn;
    }
}
