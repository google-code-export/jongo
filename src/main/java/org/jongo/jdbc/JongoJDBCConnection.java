package org.jongo.jdbc;

import javax.ws.rs.core.MultivaluedMap;
import org.jongo.enums.JDBCDriver;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface JongoJDBCConnection {
    
    public void loadDriver();
    
    public String getInsertQuery(final String table, final MultivaluedMap<String,String> params);
    
    public String getUpdateQuery(final String table, final String key, final MultivaluedMap<String,String> params);
    
    public String getDeleteQuery(final String table, final String key);
    
    public String getUrl();

    public String getUsername();
    
    public String getPassword();
    
    public JDBCDriver getDriver();
}
