package org.jongo.jdbc;

import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;
import org.jongo.enums.JDBCDriver;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class AbstractJDBCConnection {
    
    protected String url;
    protected String username;
    protected String password;
    protected JDBCDriver driver;
    
    public String getInsertQuery(final String table, final MultivaluedMap<String,String> params){
        final StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(table);
        query.append("(");
        query.append(StringUtils.join(params.keySet(), ","));
        query.append(") VALUES (");
        query.append(StringUtils.removeEnd(StringUtils.repeat("?,", params.size()), ","));
        query.append(")");
        return query.toString();
    }
    
    public String getUpdateQuery(final String table, final String key, final MultivaluedMap<String,String> params){
        final StringBuilder query = new StringBuilder("UPDATE ");
        query.append(table);
        query.append(" SET ");

        for(String k : params.keySet()){
            query.append(k); query.append(" = ?,");
        }
        
        query.deleteCharAt(query.length() - 1);
        
        query.append(" WHERE ");
        query.append(key);
        query.append(" = ?");
        return query.toString();
    }
    
    public String getDeleteQuery(final String table, final String key){
         final StringBuilder query = new StringBuilder("DELETE FROM ");
         query.append(table);
         query.append(" WHERE ");
         query.append(key);
         query.append(" = ?");
         return query.toString();
    }
    
    public JDBCDriver getDriver() {
        return driver;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }
}
