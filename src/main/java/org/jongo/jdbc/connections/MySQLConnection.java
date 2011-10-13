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
public class MySQLConnection extends AbstractJDBCConnection implements JongoJDBCConnection {
    
    private static final Logger l = LoggerFactory.getLogger(MySQLConnection.class);

    public MySQLConnection(final String url, final String user, final String password){
        this.url = url;
        this.username = user;
        this.password = password;
        this.driver = JDBCDriver.MySQL;
    }

    @Override
    public void loadDriver() {
        l.debug("Loading MySQL Driver " + this.driver.getName());
        try {
            Class.forName(this.driver.getName());
        } catch (ClassNotFoundException ex) {
            l.error("Unable to load driver. Add the MySQL Connector jar to the lib folder");
        }
    }

    @Override
    public String getCreateJongoTableQuery() {
        return "CREATE TABLE JongoTable ( id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name VARCHAR(50) UNIQUE NOT NULL, customId VARCHAR(10), permits INTEGER )";
    }

    @Override
    public String getCreateJongoQueryTable() {
        return "CREATE TABLE JongoQuery ( id INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(id), name VARCHAR(50) UNIQUE NOT NULL, query VARCHAR NOT NULL, description VARCHAR(50) )";
    }
}
