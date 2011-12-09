package org.jongo.jdbc;

import java.sql.SQLException;
import org.jongo.JongoConfiguration;
import org.jongo.jdbc.exceptions.HSQLException;
import org.jongo.jdbc.exceptions.JongoJDBCException;
import org.jongo.jdbc.exceptions.MySQLException;
import org.jongo.jdbc.exceptions.OracleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoJDBCExceptionFactory {
    
    private static final Logger l = LoggerFactory.getLogger(JongoJDBCExceptionFactory.class);
    private static final JongoConfiguration configuration = JongoConfiguration.instanceOf();
    
    public static JongoJDBCException getException(final String msg, final SQLException e) {
        l.debug("Throwing JDBC Exception with id " + e.getErrorCode());
        switch (configuration.getDriver()) {
            case MySQL:
                return new MySQLException(msg, e);
            case HSQLDB:
                return new HSQLException(msg, e);
            case ORACLE:
                return new OracleException(msg, e);
            default:
                throw new IllegalArgumentException("Not implemented yet");
        }
    }

    public static JongoJDBCException getException(final String msg, final int e) {
        l.debug("Throwing JDBC Exception with id " + e);
        switch (configuration.getDriver()) {
            case MySQL:
                return new MySQLException(msg, e);
            case HSQLDB:
                return new HSQLException(msg, e);
            case ORACLE:
                return new OracleException(msg, e);
            default:
                throw new IllegalArgumentException("Not implemented yet");
        }
    }
}
