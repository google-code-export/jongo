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
import org.jongo.JongoConfiguration;
import org.jongo.JongoUtils;
import org.jongo.demo.Demo;
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
    private static final JongoConfiguration configuration = JongoConfiguration.instanceOf();
    private static final JongoJDBCConnection connection = JDBCConnectionFactory.getJongoAdminJDBCConnection();
    private static final QueryRunner runner = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
    
    private static <T> T queryRunnerWrapper(final String query, final ResultSetHandler<T> resultSet, final Object... params) throws JongoJDBCException{
        T response = null;
        if(resultSet == null){
            l.debug("Executing update [" + query + " ] params: " + params);
            try {
                int results = runner.update(query, params);
                response = (T)new Integer(results);
            } catch (SQLException ex) {
                throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
            }
        }else{
            l.debug("Executing query [" + query + " ] params: " + params);
            try {
                response = runner.query(query, resultSet, params);
            } catch (SQLException ex) {
                throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
            }
        }
        return response;
    }
    
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
    
    public static JongoTable getJongoTable(final String table) throws JongoJDBCException{
        ResultSetHandler<JongoTable> rh = new JongoTableResultSetHandler();
        JongoTable result = queryRunnerWrapper(JongoTable.GET, rh, table);
        
        if(result == null){
            l.debug("Table " + table + " is not in JongoTables. Access Denied");
            throw JongoJDBCExceptionFactory.getException("Table " + table + " is not in JongoTables. Access Denied", JongoJDBCException.ILLEGAL_ACCESS_CODE);
        }

        if(result != null && StringUtils.isEmpty(result.getCustomId())){
            l.debug("Table " + table + " has no customId. Using default (id)");
            result.setCustomId("id");
        }
        return result;
    }
    
    public static JongoQuery getJongoQuery(final String name) throws JongoJDBCException{
        ResultSetHandler<JongoQuery> rh = new JongoQueryResultSetHandler();
        return queryRunnerWrapper(JongoQuery.GET, rh, name);
    }
    
    public static void createJongoTablesAndData() throws JongoJDBCException{
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
        String query = "SELECT * FROM JongoTable";
        List<RowResponse> results = queryRunnerWrapper(query, res);
        
        if(results != null){
            l.info("No need to create admin tables");
        }else{
            l.info("Creating Jongo Tables");
            queryRunnerWrapper(JongoUtils.createJongoTableQuery, null);
            queryRunnerWrapper(JongoUtils.createJongoQueryTableQuery, null);
        }
        
        if(configuration.isDemoModeActive()){
            Demo.generateDemoDatabase();
        }
    }
    
    public static Integer insert(final String table, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        List<String> params = new ArrayList<String>(formParams.size());
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        
        String query = connection.getInsertQuery(table, formParams);
        return queryRunnerWrapper(query, null, params);
    }
    
    public static List<RowResponse> find(final String table, final String query, Object... params) throws JongoJDBCException {
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        return queryRunnerWrapper(query, res, params);
    }
    
    public static Integer update(final String table, final String id, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        List<String> params = new ArrayList<String>(formParams.size());
        
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        params.add(id);
        
        String query = connection.getUpdateQuery(table, "id", formParams);
        return queryRunnerWrapper(query, null, params);
    }
    
    public static Integer delete(final String table, final String id) throws JongoJDBCException {
        String query = connection.getDeleteQuery(table, "id");
        return queryRunnerWrapper(query, null, id);
    }
}
