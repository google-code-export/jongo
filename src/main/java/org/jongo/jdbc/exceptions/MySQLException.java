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

package org.jongo.jdbc.exceptions;

import java.sql.SQLException;

/**
 * Subclass of DatabaseException with specific knowledge of various MySQL sql error codes.
 * @author Jeff Smith (jeff@SoftTechDesign.com, www.SoftTechDesign.com)
 */
public class MySQLException extends JongoJDBCException {

    /**
     * MySQLException Constructor
     * @param msg exception message
     * @param e SQLException
     */
    public MySQLException(String msg, SQLException e) {
        super(msg, e);
    }

    /**
     * MySQLException Constructor
     * @param msg exception message
     */
    public MySQLException(String msg) {
        super(msg);
    }
    
    public MySQLException(String msg, int e) {
        super(msg,e);
    }

    /**
     * Was db exception caused by a data integrity violation
     * @return true or false
     */
    @Override
    public boolean isDataIntegrityViolation() {
        switch (sqlErrorCode) {
            case 1217:
                return (true);
            case 1451:
                return (true);
            default:
                return (false);
        }
    }

    /**
     * Was db exception caused by a duplicate record (unique constraint) violation
     */
    @Override
    public boolean isUniqueConstraintViolation() {
        return ((sqlErrorCode == 1062) || (sqlErrorCode == 1022));
    }

    /**
     * Was db exception caused by bad sql grammer (a typo)
     * return true or false
     */
    @Override
    public boolean isBadSQLGrammar() {
        return (sqlErrorCode == 1064);
    }

    /**
     * Was db exception caused by referencing a non existent table or view
     * @return true or false
     */
    @Override
    public boolean isNonExistentTableOrViewOrCol() {
        switch (sqlErrorCode) {
            case 1146:
            case 1054:
                return (true);
            default:
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
        return (sqlErrorCode == 64);
    }

    /**
     * Was db exception caused by a row lock error or some other sql querty timeout
     * return true or false
     */
    @Override
    public boolean isRowlockOrTimedOut() {
        switch (sqlErrorCode) {
            case 1213:
            case 1205:
                return (true);
            default:
                return (false);
        }
    }

    /** was db exception caused by a an unbound variable (parameter)
     * @return boolean
     */
    @Override
    public boolean isVarParameterUnbound() {
        return (sqlErrorCode == 0);
    }
}
