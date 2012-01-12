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
import org.apache.commons.lang.StringUtils;
import org.jongo.config.impl.HSQLDBConfiguration;
import org.jongo.config.impl.MySQLConfiguration;
import org.jongo.config.impl.OracleConfiguration;
import org.jongo.enums.JDBCDriver;
import org.jongo.jdbc.DynamicFinder;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract class with common methods to all DatabaseConfiguration objects. All
 * queries which are SQL standard should be implemented here and only, database
 * specific queries should be implemented by each separate object.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public abstract class AbstractDatabaseConfiguration {
    
    private static final Logger l = LoggerFactory.getLogger(AbstractDatabaseConfiguration.class);
    
    protected String name;
    protected JDBCDriver driver;
    protected String username;
    protected String password;
    protected String url;

    public static DatabaseConfiguration instanceForAdminInMemory(){
        DatabaseConfiguration c = new HSQLDBConfiguration("jongoAdmin", "jongoAdmin", "jongoAdmin", "jdbc:hsqldb:mem:adminDemo");
        return c;
    }
    
    public static DatabaseConfiguration instanceForAdminInFile(){
        DatabaseConfiguration c = new HSQLDBConfiguration("jongoAdmin", "jongoAdmin", "jongoAdmin", "jdbc:hsqldb:file:data/jongoAdmin");
        return c;
    }
    
    public static DatabaseConfiguration instanceOf(String name, JDBCDriver driver, String user, String password, String url){
        DatabaseConfiguration c = null;
        
        switch(driver){
            case HSQLDB:
                c = new HSQLDBConfiguration(name, user, password, url);
                break;
            case MySQL:
                c = new MySQLConfiguration(name, user, password, url);
                break;
            case ORACLE:
                c = new OracleConfiguration(name, user, password, url);
                break;
            default:
                throw new IllegalArgumentException("Not implemented yet");
        }
        c.loadDriver();
        return c;
    }
    
    public String getSelectAllFromTableQuery(final String table){
        final StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(table);
        return query.toString();
    }
    
    public String getSelectAllFromTableQuery(final String table, LimitParam limit, OrderParam order){
        final StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(table);
        query.append(" ORDER BY ");
        query.append(order.getNotNullColumn());
        query.append(" ");
        query.append(order.getDirection());
        query.append(" LIMIT ");
        query.append(limit.getLimit());
        query.append(" OFFSET ");
        query.append(limit.getStart());
        return query.toString();
    }
    
    public String getSelectAllFromTableQuery(final String table, final String idCol, LimitParam limit, OrderParam order){
        final StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(table);
        query.append(" WHERE ");
        query.append(idCol);
        query.append(" = ?");
        query.append(" ORDER BY ");
        query.append(order.getNotNullColumn());
        query.append(" ");
        query.append(order.getDirection());
        query.append(" LIMIT ");
        query.append(limit.getLimit());
        query.append(" OFFSET ");
        query.append(limit.getStart());
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
    
    public String wrapDynamicFinderQuery(final DynamicFinder finder, final LimitParam limit, final OrderParam order){
        final StringBuilder query = new StringBuilder(finder.getSql());
        query.append(" ORDER BY ");
        query.append(order.getNotNullColumn());
        query.append(" ");
        query.append(order.getDirection());
        query.append(" LIMIT ");
        query.append(limit.getLimit());
        query.append(" OFFSET ");
        query.append(limit.getStart());
        return query.toString();
    }

    public JDBCDriver getDriver() {
        return driver;
    }

    public String getName() {
        return name;
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
    
    public boolean isValid(){
        if(StringUtils.isBlank(name)){
            l.warn("Invalid database name. Check your configuration.");
            return false;
        }
        
        if(StringUtils.isBlank(username)){
            l.warn("Invalid database user. Check your configuration.");
            return false;
        }
        
        if(StringUtils.isBlank(password)){
            l.warn("Invalid database password. Check your configuration.");
            return false;
        }
        
        if(StringUtils.isBlank(url)){
            l.warn("Invalid database url. Check your configuration.");
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return "DatabaseConfiguration{" + "name=" + name + ", driver=" + driver + ", user=" + username + ", password=" + password + ", url=" + url + '}';
    }
    
    
}
