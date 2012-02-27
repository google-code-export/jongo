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
 * Subclass of DatabaseException with specific knowledge of various HSQL sql error codes.
 * Created on March 7, 2008
 * @author Jeff Smith
 */
public class HSQLException extends JongoJDBCException {

    public HSQLException(String msg) {
        super(msg);
    }
    
    public HSQLException(String msg, SQLException e) {
        super(msg,e);
    }
    
    public HSQLException(String msg, int e) {
        super(msg,e);
    }

    /**
     * Was db exception caused by bad sql grammer (a typo)
     * return true or false
     */
    @Override
    public boolean isBadSQLGrammar() {
        return (((sqlErrorCode >= 67) && (sqlErrorCode <= 71)) || sqlErrorCode == 5 || sqlErrorCode == 11 || sqlErrorCode == 12
                || sqlErrorCode == 13 || sqlErrorCode == 58 || sqlErrorCode == 74 || sqlErrorCode == 121);
    }

    /**
     * Was db exception caused by a data integrity violation
     * @return true or false
     */
    @Override
    public boolean isDataIntegrityViolation() {
        return ((sqlErrorCode == 8) || (sqlErrorCode == 177));
    }

    /**
     * Was db exception caused by database being unavailable
     * @return true or false
     */
    @Override
    public boolean isDatabaseUnavailable() {
        return (((sqlErrorCode >= 1) && (sqlErrorCode <= 4)) || sqlErrorCode == 94);
    }

    /**
     * Was db exception caused by referencing a invalid bind parameter name
     * @return true or false
     */
    @Override
    public boolean isInvalidBindVariableName() {
        return (sqlErrorCode == 216);
    }

    /**
     * Was db exception caused by referencing a non existent table or view
     * @return true or false
     */
    @Override
    public boolean isNonExistentTableOrViewOrCol() {
        return (sqlErrorCode == 22 || sqlErrorCode == 28 || sqlErrorCode == 53 || sqlErrorCode == 3603 || sqlErrorCode == 5501);
    }

    /**
     * Was db exception caused by a row lock error or some other sql query timeout
     * return true or false
     */
    @Override
    public boolean isRowlockOrTimedOut() {
        return false;
    }

    /**
     * Was db exception caused by a duplicate record (unique constraint) violation 
     */
    @Override
    public boolean isUniqueConstraintViolation() {
        return ((sqlErrorCode == 9) || (sqlErrorCode == 104));
    }

    /** was db exception caused by a an unbound variable (parameter)
     * @return boolean
     */
    @Override
    public boolean isVarParameterUnbound() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return sqlErrorCode == 456;
    }
}
