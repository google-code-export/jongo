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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jongo.JongoUtils;

/**
 * Re-map the HSQLDB error codes from org.hsqldb.error.ErrorCode.java to HTTP error codes.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class HSQLException extends JongoJDBCException {
    
    private static final long serialVersionUID = 1L;
    
    private static final List<Integer> badRequestErrors = new ArrayList<Integer>();
    private static final List<Integer> badGatewayErrors = new ArrayList<Integer>();
    private static final List<Integer> forbiddenErrors = new ArrayList<Integer>();
    private static final List<Integer> notFoundErrors = new ArrayList<Integer>();
    
    static{
        badRequestErrors.addAll(Arrays.asList(new Integer[]{8, 10, 104, 157, 177, 3500, 3501, 2100, 3201, 3800, 5502, 5504, 5513, 5, 11, 12, 13, 58, 74, 121, 5581, 81, 70, 71, 72, 5000, 5510, 5512, 216, 5546, 5505, 5506}));
        badRequestErrors.addAll(JongoUtils.range(5520, 5539));
        badGatewayErrors.addAll(JongoUtils.range(1300, 1306));
        badGatewayErrors.addAll(Arrays.asList(new Integer[]{1,2,3,4,23,61,62,63,64,65,401,402,403,404,405,406,407}));
        forbiddenErrors.addAll(Arrays.asList(new Integer[]{5501, 5503, 5507, 5508, 5509, 5545}));
        notFoundErrors.addAll(Arrays.asList(new Integer[]{5505, 5506}));
    }

    @Override
    public boolean isBadGateway() {
        return badGatewayErrors.contains(Math.abs(getErrorCode()));
    }

    @Override
    public boolean isGatewayTimeout() {
        return Math.abs(getErrorCode()) == 1351;
    }

    @Override
    public boolean isBadRequest() {
        return badRequestErrors.contains(Math.abs(getErrorCode()));
    }

    @Override
    public boolean isForbidden() {
        return forbiddenErrors.contains(Math.abs(getErrorCode()));
    }

    @Override
    public boolean isNotFound() {
        return notFoundErrors.contains(Math.abs(getErrorCode()));
    }
}