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
import org.jongo.config.DatabaseConfiguration;
import org.jongo.config.JongoConfiguration;
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
    private static final JongoConfiguration conf = JongoConfiguration.instanceOf();
    
    public static int delete(final String database, final String table, final String id) throws JongoJDBCException {
        l.debug("Deleting from " + table);
        
        JongoTable result = isWritable(database, table);
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        String query = dbconf.getDeleteQuery(table, result.getCustomId());
        l.debug(query);
        
        try {
            return run.update(query, JongoUtils.parseValue(id));
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(database, ex.getMessage(), ex);
        }
    }
    
    public static int insert(final String database, final String table, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        JongoTable jongoTable = isWritable(database, table);
        
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
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        String query = dbconf.getInsertQuery(table, formParams);
        l.debug(query);
        
        try {
            return run.update(query, JongoUtils.parseValues(params));
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(database, ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> update(final String database, final String table, final String id, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        l.debug("Updating table " + table);
        
        JongoTable result = isWritable(database, table);
        
        List<String> params = new ArrayList<String>(formParams.size());
        
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        params.add(id);
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        String query = dbconf.getUpdateQuery(table, result.getCustomId(), formParams);
        l.debug(query);
        
        List<RowResponse> results = null;
        try {
            int ret = run.update(query, JongoUtils.parseValues(params));
            if(ret != 0){
                results = get(database, table, id, new LimitParam(), new OrderParam());
            }
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(database, ex.getMessage(), ex);
        }
        return results;
    }
    
    public static List<RowResponse> get(final String database, final String table, final String id, final LimitParam limit, final OrderParam order) throws JongoJDBCException {
        JongoTable result = isReadable(database, table);
        List<RowResponse> response = null;
        
        if(order.getColumn() == null) order.setColumn(result.getCustomId());
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        
        if(StringUtils.isBlank(id)){
            String query = dbconf.getSelectAllFromTableQuery(table, limit, order);
            l.debug(query);
        
            ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
            try {
                response = run.query(query, res);
            } catch (SQLException ex) {
                throw JongoJDBCExceptionFactory.getException(database, ex.getMessage(), ex);
            }
        }else{
            String query = dbconf.getSelectAllFromTableQuery(table, result.getCustomId(), limit, order);
            l.debug(query);
        
            ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
            try {
                response =  run.query(query, res, JongoUtils.parseValue(id));
            } catch (SQLException ex) {
                throw JongoJDBCExceptionFactory.getException(database, ex.getMessage(), ex);
            }
        }
        return response;
        
    }
    
    private static JongoTable isWritable(final String database, final String table) throws JongoJDBCException{
        l.debug("Checking if table is writable " + table );
        JongoTable result = AdminJDBCExecutor.getJongoTable(database, table);
        if(!result.getPermits().isWritable()){
            l.debug("Table " + table + " is not writable. Access Denied");
            throw JongoJDBCExceptionFactory.getException(database, "Cant write to table " + table + ". Access Denied", JongoJDBCException.ILLEGAL_WRITE_CODE);
        }
        return result;
    }
    
    private static JongoTable isReadable(final String database, final String table) throws JongoJDBCException{
        l.debug("Checking if table is readable " + table );
        JongoTable result = AdminJDBCExecutor.getJongoTable(database, table);
        
        if(!result.getPermits().isReadable()){
            l.debug("Table " + table + " is not readable. Access Denied");
            throw JongoJDBCExceptionFactory.getException(database, "Cant read table " + table + ". Access Denied", JongoJDBCException.ILLEGAL_READ_CODE);
        }
        
        return result;
    }

    public static List<RowResponse> findByColumn(final String database, final String table, final String column, Object... params) throws JongoJDBCException {
        JongoTable result = isReadable(database, table);
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        String query = dbconf.getSelectAllFromTableQuery(table, column);
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
        try {
            List<RowResponse> results = run.query(query, res, params);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(database, ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> find(final String database, final DynamicFinder query, Object... params) throws JongoJDBCException{
        l.debug(query.getSql() + " params: " + JongoUtils.varargToString(params));
        
        isReadable(database, query.getTable());
        
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(query.findAll());
        try {
            List<RowResponse> results = run.query(query.getSql(), res, params);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(database, ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> getTableMetaData(final String database, final String table) throws JongoJDBCException {
        l.debug("Obtaining metadata from table " + table);
        
        isReadable(database, table);
        
        ResultSetHandler<List<RowResponse>> res = new ResultSetMetaDataHandler();
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        String query = dbconf.getFirstRowQuery(table);
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        try {
            List<RowResponse> results = run.query(query, res);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(database, ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> executeQuery(final String database, final String queryName, final Object... params) throws JongoJDBCException {
        l.debug("Executing query " + queryName + " params: " + JongoUtils.varargToString(params));
        
        JongoQuery query = AdminJDBCExecutor.getJongoQuery(database, queryName);
        if(query == null){
            return null;
        }
        
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        try {
            List<RowResponse> results = run.query(query.getCleanQuery(), res, params);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(database, ex.getMessage(), ex);
        }
    }
    
    public static void shutdown(){
        l.debug("Shutting down JDBC connections");
        try {
//            JDBCConnectionFactory.getDataSource().getConnection().close();
        } catch (Exception ex) {
            l.warn("Failed to close connection to database?");
            l.debug(ex.getMessage());
        }
    }
    
    public static List<RowResponse> getListOfTables(final String database) throws JongoJDBCException{
        l.debug("Obtaining the list of tables for the database " + database);
        if(!conf.allowListTables()){
            throw JongoJDBCExceptionFactory.getException(database, "Cant read database metadata. Access Denied", JongoJDBCException.ILLEGAL_READ_CODE);
        }
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        String query = dbconf.getListOfTablesQuery();
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        try {
            List<RowResponse> results = run.query(query, res);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(database, ex.getMessage(), ex);
        }
    }
}
