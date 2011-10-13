package org.jongo.jdbc;

import java.util.Map;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface JongoJDBCConnection {
    
    public void loadDriver();
    
    public String getInsertQuery(final String table, final Map<String,String> params);
    
    public String getUpdateQuery(final String table, final String key, final Map<String,String> params);
    
    public String getDeleteQuery(final String table, final String key);
    
    public String getCreateJongoTableQuery();
    
    public String getCreateJongoTableSequence();
    
    public String getCreateJongoTableTrigger();
    
    public String getCreateJongoQueryTable();
    
    public String getCreateJongoQuerySequence();
    
    public String getCreateJongoQueryTrigger();
    
}
