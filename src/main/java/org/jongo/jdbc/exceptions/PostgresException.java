package org.jongo.jdbc.exceptions;

import java.sql.SQLException;

/**
 * Subclass of DatabaseException with specific knowledge of various PostgreSQL sql error codes.
 * Created on 22 may 2005, 15.15
 * @author Paolo Orru (paolo.orru@gmail.com)
 */
public class PostgresException extends JongoJDBCException {

    /**
     * PostgresException Constructor
     * @param msg exception message
     * @param e SQLException
     */
    public PostgresException(String msg, SQLException e) {
        super(msg, e);
    }

    /**
     * PostgresException Constructor
     * @param msg exception message
     */
    public PostgresException(String msg) {
        super(msg);
    }

    public PostgresException(String msg, int e) {
        super(msg,e);
    }
    
    /**
     * Was db exception caused by a data integrity violation
     * @return true or false
     */
    @Override
    public boolean isDataIntegrityViolation() {
        //Class 23 - Integrity Constraint Violation
        return (sqlState.startsWith("23"));
    }

    /**
     * Was db exception caused by a duplicate record (unique constraint) violation 
     */
    @Override
    public boolean isUniqueConstraintViolation() {
        return (sqlErrorCode == 23505);
    }

    /**
     * Was db exception caused by bad sql grammer (a typo)
     * return true or false
     */
    @Override
    public boolean isBadSQLGrammar() {
        if (sqlState.equals("42000")) // SYNTAX ERROR OR ACCESS RULE VIOLATION
        {
            return (true);
        } else if (sqlState.equalsIgnoreCase("42601")) // SYNTAX ERROR
        {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Was db exception caused by referencing a non existent table or view
     * @return true or false
     */
    @Override
    public boolean isNonExistentTableOrViewOrCol() {
        if (sqlState.equalsIgnoreCase("42P01")) // UNDEFINED TABLE
        {
            return (true);
        }
        if (sqlState.equals("42703")) // UNDEFINED COLUMN
        {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Was db exception caused by referencing a invalid bind parameter name
     * @return true or false
     */
    @Override
    public boolean isInvalidBindVariableName() {
        return (false);
    }

    /**
     * Was db exception caused by database being unavailable
     * @return true or false
     */
    @Override
    public boolean isDatabaseUnavailable() {
        if (sqlState.equalsIgnoreCase("3D000")) // INVALID CATALOG NAME
        {
            return (true);
        }
        if (sqlState.startsWith("08")) // Class 08 - Connection Exception
        {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Was db exception caused by a row lock error or some other sql query timeout
     * return true or false
     */
    @Override
    public boolean isRowlockOrTimedOut() {
        return (sqlState.equalsIgnoreCase("40P01")); // DEADLOCK DETECTED
    }

    /** was db exception caused by a an unbound variable (parameter)
     * @return boolean
     */
    @Override
    public boolean isVarParameterUnbound() {
        return (sqlState.equals("22023")); // INVALID PARAMETER VALUE
    }
}
