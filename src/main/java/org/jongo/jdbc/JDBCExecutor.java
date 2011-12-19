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

import org.jongo.jdbc.exceptions.JongoJDBCExceptionFactory;
import org.jongo.handler.ResultSetMetaDataHandler;
import org.jongo.handler.JongoResultSetHandler;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.jongo.JongoUtils;
import org.jongo.domain.JongoQuery;
import org.jongo.domain.JongoTable;
import org.jongo.jdbc.exceptions.JongoJDBCException;
import org.jongo.rest.xstream.RowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JDBCExecutor {

    private static final Logger l = LoggerFactory.getLogger(JDBCExecutor.class);
    private static final JongoJDBCConnection jdbcConnection = JDBCConnectionFactory.getJongoJDBCConnection();
    private static final QueryRunner runner = new QueryRunner(JDBCConnectionFactory.getDataSource());
    
    private static <T> T queryRunnerWrapper(final String query, final ResultSetHandler<T> resultSet, final Object... params) throws JongoJDBCException{
        l.debug("Executing query [" + query + " ]params: " + params);
        T response = null;
        if(resultSet == null){
            try {
                int results = runner.update(query, params);
                response = (T)new Integer(results);
            } catch (SQLException ex) {
                throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
            }
        }else{
            try {
                response = runner.query(query, resultSet, params);
            } catch (SQLException ex) {
                throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
            }
        }
        return response;
    }
    
    public static Integer delete(final String table, final String id) throws JongoJDBCException {
        l.debug("Deleting from " + table + " registry " + id);
        JongoTable result = isWritable(table);
        String query = jdbcConnection.getDeleteQuery(table, result.getCustomId());
        return queryRunnerWrapper(query, null, JongoUtils.parseValue(id));
    }
    
    public static Integer insert(final String table, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        l.debug("Inserting in table " + table);
        JongoTable jongoTable = isWritable(table);
        
        List<String> params = new ArrayList<String>(formParams.size());
        String idToBeRemoved = null;
        for(String k : formParams.keySet()){
            if(k.equalsIgnoreCase(jongoTable.getCustomId())){
                if(!StringUtils.isBlank(formParams.getFirst(k))){
                    params.add(formParams.getFirst(k));
                }else{
                    l.info("For some reason I'm receiving and empty " + k + ". I'm removing it from the params. Are you using ExtJS?");
                    idToBeRemoved = k;
                }
            }else{
                params.add(formParams.getFirst(k));
            }
        }
        
        if(idToBeRemoved != null){
            formParams.remove(idToBeRemoved);
        }
        
        String query = jdbcConnection.getInsertQuery(table, formParams);
        return queryRunnerWrapper(query, null, JongoUtils.parseValues(params));
    }
    
    public static List<RowResponse> update(final String table, final String id, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        l.debug("Updating table " + table);
        
        JongoTable result = isWritable(table);
        
        List<String> params = new ArrayList<String>(formParams.size());
        
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        params.add(id);
        
        String query = jdbcConnection.getUpdateQuery(table, result.getCustomId(), formParams);
        return queryRunnerWrapper(query, null, JongoUtils.parseValues(params));
    }
    
    public static List<RowResponse> get(final String table, final String id, final LimitParam limit, final OrderParam order) throws JongoJDBCException {
        JongoTable result = isReadable(table);
        
        List<RowResponse> response = null;
        
        if(StringUtils.isBlank(id)){
            String query = jdbcConnection.getSelectAllFromTableQuery(table, limit, order);
            ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
            response = queryRunnerWrapper(query, res);
        }else{
            String query = jdbcConnection.getSelectAllFromTableQuery(table, result.getCustomId(), limit, order);
            ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
            response = queryRunnerWrapper(query, res);
        }
        return response;
        
    }
    
    private static JongoTable isWritable(final String table) throws JongoJDBCException{
        l.debug("Checking if table is writable " + table );
        JongoTable result = AdminJDBCExecutor.getJongoTable(table);
        
        if(!result.getPermits().isWritable()){
            l.debug("Table " + table + " is not writable. Access Denied");
            throw JongoJDBCExceptionFactory.getException("Cant write to table " + table + ". Access Denied", JongoJDBCException.ILLEGAL_WRITE_CODE);
        }
        
        return result;
    }
    
    private static JongoTable isReadable(final String table) throws JongoJDBCException{
        l.debug("Checking if table is readable " + table );
        JongoTable result = AdminJDBCExecutor.getJongoTable(table);

        if(!result.getPermits().isReadable()){
            l.debug("Table " + table + " is not readable. Access Denied");
            throw JongoJDBCExceptionFactory.getException("Cant read table " + table + ". Access Denied", JongoJDBCException.ILLEGAL_READ_CODE);
        }
        
        return result;
    }

    public static List<RowResponse> findByColumn(final String table, final String column, Object... params) throws JongoJDBCException {
        isReadable(table);
        String query = jdbcConnection.getSelectAllFromTableQuery(table, column);
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
        return queryRunnerWrapper(query, res, params);
    }
    
    public static List<RowResponse> find(final DynamicFinder query, Object... params) throws JongoJDBCException{
        l.debug(query.getSql() + " params: " + JongoUtils.varargToString(params));
        
        isReadable(query.getTable());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(query.findAll());
        return queryRunnerWrapper(query.getSql(), res, params);
    }

    public static List<RowResponse> getTableMetaData(final String table) throws JongoJDBCException {
        l.debug("Obtaining metadata from table " + table);
        
        isReadable(table);
        
        ResultSetHandler<List<RowResponse>> res = new ResultSetMetaDataHandler();
        String query = jdbcConnection.getFirstRowQuery(table);
        return queryRunnerWrapper(query, res);
    }
    
    public static List<RowResponse> executeQuery(final String queryName, final Object... params) throws JongoJDBCException {
        l.debug("Executing query " + queryName + " params: " + JongoUtils.varargToString(params));
        
        JongoQuery query = AdminJDBCExecutor.getJongoQuery(queryName);
        if(query == null){
            return null;
        }
        
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        return queryRunnerWrapper(query.getCleanQuery(), res, params);
    }
    
    public static void shutdown(){
        l.debug("Shutting down JDBC connections");
        try {
            JDBCConnectionFactory.getDataSource().getConnection().close();
        } catch (Exception ex) {
            l.warn("Failed to close connection to database?");
            l.debug(ex.getMessage());
        }
    }
}
