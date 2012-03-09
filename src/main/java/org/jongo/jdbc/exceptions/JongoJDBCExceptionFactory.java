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
import org.jongo.config.JongoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
@Deprecated
public class JongoJDBCExceptionFactory {
    
    private static final Logger l = LoggerFactory.getLogger(JongoJDBCExceptionFactory.class);
    private static final JongoConfiguration configuration = JongoConfiguration.instanceOf();
    
    public static JongoJDBCException getException(final String database, final SQLException e) {
        l.debug("Throwing JDBC Exception with id " + e.getErrorCode());
        JongoJDBCException ex;
        switch (configuration.getDriver(database)) {
            case HSQLDB:
                ex = (HSQLException) e; break;
            default:
                throw new IllegalArgumentException("Not implemented yet");
        }
        return ex;
    }

}
