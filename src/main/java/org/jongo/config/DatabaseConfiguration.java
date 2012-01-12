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
package org.jongo.config;

import javax.ws.rs.core.MultivaluedMap;
import org.jongo.enums.JDBCDriver;
import org.jongo.jdbc.DynamicFinder;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;

/**
 * Interface for DatabaseConfiguration objects. Some of this methods are
 * implemented by the AbstractDatabaseConfiguration class which is common
 * to all implementations. Each implementation should override the
 * appropriate methods which are specific of its database.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface DatabaseConfiguration {
    
    /**
     * Loads the corresponding JDBC driver of the database in the classpath.
     */
    public void loadDriver();
    
    /**
     * Creates a query which returns all objects from a table
     * @param table the name of the table to query
     * @return the query String, i.e. SELECT * FROM foo
     */
    public String getSelectAllFromTableQuery(final String table);
    
    /**
     * Creates a query which returns all objects from a table with the given
     * limit and order parameters.
     * @param table the name of the table to query
     * @param limit the limit parameters
     * @param order the order parameters
     * @return the query String, i.e. SELECT * FROM foo ORDER BY id LIMIT 25 OFFSET 0
     */
    public String getSelectAllFromTableQuery(final String table, LimitParam limit, OrderParam order);
    
    /**
     * Creates a parameterized query which returns all objects from a table
     * using a custom column value.
     * @param table the name of the table to query
     * @param idCol the name of the column to use in the query
     * @return the query String, i.e. SELECT * FROM foo WHERE bar = ?
     */
    public String getSelectAllFromTableQuery(final String table, final String idCol);
    
    /**
     * Creates a parameterized query which returns all objects from a table with the given
     * limit and order parameters using a custom column.
     * @param table the name of the table to query
     * @param idCol the name of the column to use in the query
     * @param limit the limit parameters
     * @param order the order parameters
     * @return the query String, i.e. SELECT * FROM foo WHERE bar = ? ORDER BY id LIMIT 25 OFFSET 0
     */
    public String getSelectAllFromTableQuery(final String table, final String idCol, LimitParam limit, OrderParam order);
    
    /**
     * Generates an INSERT parameterized query on the given table and with
     * the given parameters
     * @param table the name of the table where the insert is performed
     * @param params the parameters to generate the parameterized query
     * @return the parameterized query String, i.e. INSERT INTO foo (a,b,c,d) VALUES (?,?,?,?)
     */
    public String getInsertQuery(final String table, final MultivaluedMap<String,String> params);
    
    /**
     * Generates an UPDATE parameterized query on the given table and key with
     * the given parameters
     * @param table the name of the table where the update is performed
     * @param key the column to use in the where clause
     * @param params the parameters to generate the parameterized query
     * @return the parameterized query String, i.e. UPDATE foo SET (a=?,b=?,c=?,d=?) WHERE key = ?
     */
    public String getUpdateQuery(final String table, final String key, final MultivaluedMap<String,String> params);
    
    /**
     * Generates a DELETE parameterized query on the given table and key
     * @param table the name of the table where the update is performed
     * @param key the column to use in the where clause
     * @return the parameterized query String, i.e. DELETE FROM foo WHERE key = ?
     */
    public String getDeleteQuery(final String table, final String key);
    
    public String getUrl();

    public String getUsername();
    
    public String getPassword();
    
    public JDBCDriver getDriver();
    
    /**
     * Implements a string which should only return the first row of a table. Since every RDBMS supports a different syntax we have
     * to implement it for each connection. From the wikipedia: http://en.wikipedia.org/wiki/Select_(SQL)#FETCH_FIRST_clause
     * SELECT * FROM T FETCH FIRST 10 ROWS ONLY
     * This clause currently is supported by IBM DB2, Sybase SQL Anywhere, PostgreSQL, EffiProz and HSQLDB version 2.0.
     * SELECT * FROM T LIMIT 10 OFFSET 20	Netezza, MySQL, PostgreSQL (also supports the standard, since version 8.4), SQLite, HSQLDB, H2
     * SELECT * from T WHERE ROWNUM <= 10	Oracle (also supports the standard, since Oracle8i)
     * @param table
     * @return a query that when executed should only return the first row of a table.
     */
    public String getFirstRowQuery(final String table);
    
    /**
     * Generates a query specific to evert database to obtain the list of tables of the current
     * database. Try to implement the query in such a way that the key for the table name is "table_name".
     * @return a query that when executed should return a list with the tables of the current database.
     */
    public String getListOfTablesQuery();
    
    /**
     * Wraps the query generated by a dynamic finder with the limit and order parameters.
     * @param finder where the query comes from
     * @param limit the limit parameters
     * @param order the order parameters
     * @return the same query generated by the finder but wrapped with limit and order.
     */
    public String wrapDynamicFinderQuery(final DynamicFinder finder, final LimitParam limit, final OrderParam order);
}
