package org.jongo.jdbc;

import javax.ws.rs.core.MultivaluedMap;
import org.jongo.enums.JDBCDriver;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface JongoJDBCConnection {
    
    public void loadDriver();
    
    public String getSelectAllFromTableQuery(final String table);
    
    public String getSelectAllFromTableQuery(final String table, final String idCol);
    
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
