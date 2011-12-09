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
    
    public String getSelectAllFromTableQuery(final String table){
        final StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(table);
        return query.toString();
    }
    
    public String getSelectAllFromTableQuery(final String table, final String idCol){
        final StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(table);
        query.append(" WHERE ");
        query.append(idCol);
        query.append(" = ?");
        return query.toString();
    }
    
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
    
    public String getFirstRowQuery(String table) {
        final StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(table);
        query.append(" FETCH FIRST 1 ROW ONLY");
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
