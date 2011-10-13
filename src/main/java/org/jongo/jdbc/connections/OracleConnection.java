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
public class OracleConnection extends AbstractJDBCConnection implements JongoJDBCConnection{
    
    private static final Logger l = LoggerFactory.getLogger(OracleConnection.class);
    
    public OracleConnection(final String url, final String user, final String password) {
        this.url = url;
        this.username = user;
        this.password = password;
        this.driver = JDBCDriver.ORACLE;
    }
    
    @Override
    public void loadDriver() {
        l.debug("Loading Oracle Driver " + this.driver.getName());
        try {
            Class.forName(this.driver.getName());
        } catch (ClassNotFoundException ex) {
            l.error("Unable to load driver. Add the Oracle JDBC Connector jar to the lib folder");
        }
    }

    @Override
    public String getCreateJongoTableQuery() {
        return "CREATE TABLE JongoTable ( id INTEGER, name VARCHAR(50) UNIQUE NOT NULL, customId VARCHAR(10), permits INTEGER) ";
    }
    
    @Override
    public String getCreateJongoTableSequence() {
        return "CREATE SEQUENCE jongo_table_seq START WITH 0 INCREMENT BY 1 NOMAXVALUE";
    }

    @Override
    public String getCreateJongoTableTrigger() {
        return "CREATE TRIGGER jongo_table_trigger BEFORE INSERT ON JongoTable FOR EACH ROW BEGIN SELECT jongo_table_seq.nextval INTO :new.id FROM DUAL";
    }
    
    @Override
    public String getCreateJongoQueryTable() {
        return "CREATE TABLE JongoQuery ( id INTEGER, name VARCHAR(50) UNIQUE NOT NULL, query VARCHAR NOT NULL, description VARCHAR(50) )";
    }

    @Override
    public String getCreateJongoQuerySequence() {
        return "CREATE SEQUENCE jongo_query_seq START WITH 0 INCREMENT BY 1 NOMAXVALUE";
    }
    
    @Override
    public String getCreateJongoQueryTrigger() {
        return "CREATE TRIGGER jongo_query_trigger BEFORE INSERT ON JongoQuery FOR EACH ROW BEGIN SELECT jongo_query_seq.nextval INTO :new.id FROM DUAL";
    }
}
