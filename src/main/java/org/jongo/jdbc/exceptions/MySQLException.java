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

public class MySQLException extends JongoJDBCException {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isBadGateway() {
        switch (this.getErrorCode()) {
            case 64: return true;
            default:   return (false);
        }
    }

    @Override
    public boolean isGatewayTimeout() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isBadRequest() {
        switch (this.getErrorCode()) {
            case 1217: return true;
            case 1451: return true;
            case 1064: return true;
            case 5501: return true;
            case 1062: return true;
            case 1022: return true;
            default:   return (false);
        }
    }

    @Override
    public boolean isForbidden() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isNotFound() {
        switch (this.getErrorCode()) {
            case 1146:
            case 1054:
            case 1364:
                return (true);
            default:
                return (false);
        }
    }
}
