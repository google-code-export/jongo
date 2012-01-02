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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.jongo.JongoUtils;
import org.jongo.config.DatabaseConfiguration;
import org.jongo.config.JongoConfiguration;
import org.jongo.domain.JongoQuery;
import org.jongo.domain.JongoTable;
import org.jongo.handler.JongoQueryResultSetHandler;
import org.jongo.handler.JongoResultSetHandler;
import org.jongo.handler.JongoTableResultSetHandler;
import org.jongo.jdbc.exceptions.JongoJDBCException;
import org.jongo.jdbc.exceptions.JongoJDBCExceptionFactory;
import org.jongo.rest.xstream.RowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for Administration Console related operations.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class AdminJDBCExecutor {
    private static final Logger l = LoggerFactory.getLogger(JDBCExecutor.class);
    private static final JongoConfiguration conf = JongoConfiguration.instanceOf();
    private static final DatabaseConfiguration dbconf = conf.getAdminDatabaseConfiguration();
    private static final QueryRunner run = JDBCConnectionFactory.getAdminQueryRunner();
    
    /**
     * Shutdown the connection with the Administration database.
     */
    public static void shutdown(){
        l.debug("Shutting down JDBC connections");
        try {
            JDBCConnectionFactory.getAdminDataSource().getConnection().close();
        } catch (Exception ex) {
            l.warn("Failed to close admin connection to database?");
            l.debug(ex.getMessage());
        }
    }
    
    public static JongoTable getJongoTable(final String database, final String table) throws JongoJDBCException{
        ResultSetHandler<JongoTable> rh = new JongoTableResultSetHandler();
        JongoTable result = null;
        try {
            result = run.query(JongoTable.GET, rh, table, database);
        } catch (SQLException ex) {
            l.debug(ex.getMessage());
        }
        
        if(result == null){
            l.debug("Table " + database + "." + table + " is not in JongoTables. Access Denied");
            l.debug("Table " + database + "." + table + " is not readable. Access Denied");
            throw JongoJDBCExceptionFactory.getException(database, "Cant read table " + database + "." + table + ". Access Denied", JongoJDBCException.ILLEGAL_READ_CODE);
        }

        if(result != null && StringUtils.isEmpty(result.getCustomId())){
            l.debug("Table " + table + " has no customId. Using default (id)");
            result.setCustomId("id");
        }
        return result;
    }
    
    public static JongoQuery getJongoQuery(final String database, final String name) throws JongoJDBCException{
        ResultSetHandler<JongoQuery> rh = new JongoQueryResultSetHandler();
        JongoQuery result = null;
        try {
            result = run.query(JongoQuery.GET, rh, name, database);
        } catch (SQLException ex) {
            l.debug(ex.getMessage());
        }
        
        return result;
    }
    
    public static void createJongoTablesAndData() throws SQLException{
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
        String query = dbconf.getSelectAllFromTableQuery("JongoTable");
        List<RowResponse> results = null;
        try {
            results = run.query(query, res);
        } catch (SQLException ex) {}
        
        if(results != null){
            l.info("No need to create admin tables");
        }else{
            l.info("Creating Jongo Tables");
            update(JongoUtils.getCreateJongoTableQuery());
            update(JongoUtils.getCreateJongoQueryTableQuery());
            update(JongoQuery.CREATE, "jongoTest", "jongoTest", "", "This is the holder for adminconsole test button");
        }
    }
    
    private static int update(final String query, final Object... params) throws SQLException {
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        return run.update(query, params);
    }
    
    public static int insert(final String table, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        l.debug("Inserting in admin " + table);
        
        List<String> params = new ArrayList<String>(formParams.size());
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        
        String query = dbconf.getInsertQuery(table, formParams);
        l.debug(query);
        
        try {
            return run.update(query, JongoUtils.parseValues(params));
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> find(final String table, Object... params) throws JongoJDBCException {
        String query = dbconf.getSelectAllFromTableQuery(table, "id");
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        try {
            List<RowResponse> results = run.query(query, res, params);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> findAll(final String table) throws JongoJDBCException {
        String query = dbconf.getSelectAllFromTableQuery(table);
        l.debug(query);
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        try {
            List<RowResponse> results = run.query(query, res);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static int update(final String table, final String id, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        l.debug("Updating admin table " + table);
        
        List<String> params = new ArrayList<String>(formParams.size());
        
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        params.add(id);
        
        String query = dbconf.getUpdateQuery(table, "id", formParams);
        l.debug(query);
        try {
            return run.update(query, JongoUtils.parseValues(params));
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static int delete(final String table, final String id) throws JongoJDBCException {
        l.debug("Deleting admin " + table);
        
        String query = dbconf.getDeleteQuery(table, "id");
        l.debug(query);
        
        try {
            return run.update(query, JongoUtils.parseValue(id));
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
}
