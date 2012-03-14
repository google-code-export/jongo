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

import java.sql.*;
import org.jongo.handler.ResultSetMetaDataHandler;
import org.jongo.handler.JongoResultSetHandler;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.jongo.JongoUtils;
import org.jongo.config.DatabaseConfiguration;
import org.jongo.config.JongoConfiguration;
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
    
    public static int delete(final QueryParams params) throws SQLException {
        l.debug("Deleting from " + params.getTable());
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(params.getDatabase());
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(params.getDatabase());
        String query = dbconf.getDeleteQuery(params.getTable(), params.getIdField());
        l.debug(query);
        
        try {
            return run.update(query, JongoUtils.parseValue(params.getId()));
        } catch (SQLException ex) {
            throw ex;
        }
    }
    
    public static int insert(final QueryParams queryParams) throws SQLException {
        
        if(!queryParams.isValid())
            throw new IllegalArgumentException("Invalid QueryParams");
        
        List<String> params = new ArrayList<String>(queryParams.getParams().size());
        String idToBeRemoved = null;
        for(String k : queryParams.getParams().keySet()){
            if(k.equalsIgnoreCase(queryParams.getIdField())){
                if(!StringUtils.isBlank(queryParams.getParams().get(k))){
                    params.add(queryParams.getParams().get(k));
                }else{
                    l.info("For some reason I'm receiving an empty " + k + ". I'm removing it from the params. Are you using ExtJS?");
                    idToBeRemoved = k;
                }
            }else{
                params.add(queryParams.getParams().get(k));
            }
        }
        
        if(idToBeRemoved != null){
            queryParams.getParams().remove(idToBeRemoved);
        }
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(queryParams.getDatabase());
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(queryParams.getDatabase());
        String query = dbconf.getInsertQuery(queryParams.getTable(), queryParams.getParams());
        l.debug(query);
        
        try {
            return run.update(query, JongoUtils.parseValues(params));
        } catch (SQLException ex) {
            throw ex;
        }
    }
    
    public static List<RowResponse> update(final QueryParams queryParams) throws SQLException {
        
        if(!queryParams.isValid())
            throw new IllegalArgumentException("Invalid QueryParams");
        
        l.debug("Updating table " + queryParams.getTable());
        
        List<String> params = new ArrayList<String>(queryParams.getParams().size());
        
        for(String k : queryParams.getParams().keySet()){
            params.add(queryParams.getParams().get(k));
        }
        params.add(queryParams.getId());
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(queryParams.getDatabase());
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(queryParams.getDatabase());
        String query = dbconf.getUpdateQuery(queryParams.getTable(), queryParams.getIdField(), queryParams.getParams());
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        
        List<RowResponse> results = null;
        try {
            int ret = run.update(query, JongoUtils.parseValues(params));
            if(ret != 0){
                results = get(queryParams);
            }
        } catch (SQLException ex) {
            throw ex;
        }
        return results;
    }
    
    public static List<RowResponse> get(final QueryParams params) throws SQLException {
        List<RowResponse> response = null;
        
        if(params.getOrder().getColumn() == null) params.getOrder().setColumn(params.getIdField());
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(params.getDatabase());
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(params.getDatabase());
        
        if(StringUtils.isBlank(params.getId())){
            String query = dbconf.getSelectAllFromTableQuery(params.getTable(), params.getLimit(), params.getOrder());
            l.debug(query);
        
            ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
            try {
                response = run.query(query, res);
            } catch (SQLException ex) {
                throw ex;
            }
        }else{
            String query = dbconf.getSelectAllFromTableQuery(params.getTable(), params.getIdField(), params.getLimit(), params.getOrder());
            l.debug(query + " params: " + params.toString());
        
            ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
            try {
                response =  run.query(query, res, JongoUtils.parseValue(params.getId()));
            } catch (SQLException ex) {
                throw ex;
            }
        }
        return response;
        
    }
    
    public static List<RowResponse> findByColumn(final QueryParams queryParams, Object... params) throws SQLException {
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(queryParams.getDatabase());
        String query = dbconf.getSelectAllFromTableQuery(queryParams.getTable(), queryParams.getIdField(), queryParams.getLimit(), queryParams.getOrder());
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(queryParams.getDatabase());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        try {
            List<RowResponse> results = run.query(query, res, params);
            return results;
        } catch (SQLException ex) {
            throw ex;
        }
    }
    
    public static List<RowResponse> find(final String database, final DynamicFinder df, final LimitParam limit, final OrderParam order, Object... params) throws SQLException{
        l.debug(df.getSql() + " params: " + JongoUtils.varargToString(params));
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        String query = dbconf.wrapDynamicFinderQuery(df, limit, order);
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        try {
            List<RowResponse> results = run.query(query, res, params);
            return results;
        } catch (SQLException ex) {
            throw ex;
        }
    }
    
    public static List<RowResponse> getTableMetaData(final QueryParams params) throws SQLException {
        l.debug("Obtaining metadata from table " + params.getTable());
        
        ResultSetHandler<List<RowResponse>> res = new ResultSetMetaDataHandler();
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(params.getDatabase());
        String query = dbconf.getFirstRowQuery(params.getTable());
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(params.getDatabase());
        try {
            List<RowResponse> results = run.query(query, res);
            return results;
        } catch (SQLException ex) {
            throw ex;
        }
    }
    
    public static List<RowResponse> executeQuery(final String database, final String queryName, final List<StoredProcedureParam> params) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }
    
    public static void shutdown(){
        l.debug("Shutting down JDBC connections");
        try {
            JDBCConnectionFactory.closeConnections();
        } catch (Exception ex) {
            l.warn("Failed to close connection to database?");
            l.debug(ex.getMessage());
        }
    }
    
    public static List<RowResponse> getListOfTables(final String database) throws SQLException{
        l.debug("Obtaining the list of tables for the database " + database);
//        if(!conf.allowListTables()){
//            throw JongoJDBCExceptionFactory.getException(database, "Cant read database metadata. Access Denied", JongoJDBCException.ILLEGAL_READ_CODE);
//        }
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        String query = dbconf.getListOfTablesQuery();
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        try {
            List<RowResponse> results = run.query(query, res);
            return results;
        } catch (SQLException ex) {
            throw ex;
        }
    }
}
