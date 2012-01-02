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

package org.jongo.jdbc;

import javax.ws.rs.core.MultivaluedMap;
import org.jongo.enums.JDBCDriver;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
@Deprecated
public interface JongoJDBCConnection {
    
    public void loadDriver();
    
    public String getSelectAllFromTableQuery(final String table);
    
    public String getSelectAllFromTableQuery(final String table, LimitParam limit, OrderParam order);
    
    public String getSelectAllFromTableQuery(final String table, final String idCol);
    
    public String getSelectAllFromTableQuery(final String table, final String idCol, LimitParam limit, OrderParam order);
    
    public String getInsertQuery(final String table, final MultivaluedMap<String,String> params);
    
    public String getUpdateQuery(final String table, final String key, final MultivaluedMap<String,String> params);
    
    public String getDeleteQuery(final String table, final String key);
    
    public String getUrl();

    public String getUsername();
    
    public String getPassword();
    
    public JDBCDriver getDriver();
    
    /**
     * Implements a string which should return only return the first row of a table. Since every RDBMS supports a different syntax we have
     * to implement it for each connection. From the wikipedia: http://en.wikipedia.org/wiki/Select_(SQL)#FETCH_FIRST_clause
     * SELECT * FROM T FETCH FIRST 10 ROWS ONLY
     * This clause currently is supported by IBM DB2, Sybase SQL Anywhere, PostgreSQL, EffiProz and HSQLDB version 2.0.
     * SELECT * FROM T LIMIT 10 OFFSET 20	Netezza, MySQL, PostgreSQL (also supports the standard, since version 8.4), SQLite, HSQLDB, H2
     * SELECT * from T WHERE ROWNUM <= 10	Oracle (also supports the standard, since Oracle8i)
     * @param table
     * @return a query that when executed should only return the first row of a table.
     */
    public String getFirstRowQuery(final String table);
}
