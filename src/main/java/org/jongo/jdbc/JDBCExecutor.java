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

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.jongo.JongoUtils;
import org.jongo.config.DatabaseConfiguration;
import org.jongo.config.JongoConfiguration;
import org.jongo.handler.JongoResultSetHandler;
import org.jongo.handler.ResultSetMetaDataHandler;
import org.jongo.rest.xstream.Row;
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
    
    public static List<Row> update(final QueryParams queryParams) throws SQLException {
        
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
        
        List<Row> results = null;
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
    
    public static List<Row> get(final QueryParams params) throws SQLException {
        List<Row> response = null;
        
        if(params.getOrder().getColumn() == null) params.getOrder().setColumn(params.getIdField());
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(params.getDatabase());
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(params.getDatabase());
        
        if(StringUtils.isBlank(params.getId())){
            String query = dbconf.getSelectAllFromTableQuery(params.getTable(), params.getLimit(), params.getOrder());
            l.debug(query);
        
            ResultSetHandler<List<Row>> res = new JongoResultSetHandler(true);
            try {
                response = run.query(query, res);
            } catch (SQLException ex) {
                throw ex;
            }
        }else{
            String query = dbconf.getSelectAllFromTableQuery(params.getTable(), params.getIdField(), params.getLimit(), params.getOrder());
            l.debug(query + " params: " + params.toString());
        
            ResultSetHandler<List<Row>> res = new JongoResultSetHandler(false);
            try {
                response =  run.query(query, res, JongoUtils.parseValue(params.getId()));
            } catch (SQLException ex) {
                throw ex;
            }
        }
        return response;
        
    }
    
    public static List<Row> findByColumn(final QueryParams queryParams, Object... params) throws SQLException {
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(queryParams.getDatabase());
        String query = dbconf.getSelectAllFromTableQuery(queryParams.getTable(), queryParams.getIdField(), queryParams.getLimit(), queryParams.getOrder());
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(queryParams.getDatabase());
        ResultSetHandler<List<Row>> res = new JongoResultSetHandler(true);
        try {
            List<Row> results = run.query(query, res, params);
            return results;
        } catch (SQLException ex) {
            throw ex;
        }
    }
    
    public static List<Row> find(final String database, final DynamicFinder df, final LimitParam limit, final OrderParam order, Object... params) throws SQLException{
        l.debug(df.getSql() + " params: " + JongoUtils.varargToString(params));
        
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        String query = dbconf.wrapDynamicFinderQuery(df, limit, order);
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        ResultSetHandler<List<Row>> res = new JongoResultSetHandler(true);
        try {
            List<Row> results = run.query(query, res, params);
            return results;
        } catch (SQLException ex) {
            throw ex;
        }
    }
    
    public static List<Row> getTableMetaData(final QueryParams params) throws SQLException {
        l.debug("Obtaining metadata from table " + params.getTable());
        
        ResultSetHandler<List<Row>> res = new ResultSetMetaDataHandler();
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(params.getDatabase());
        String query = dbconf.getFirstRowQuery(params.getTable());
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(params.getDatabase());
        try {
            List<Row> results = run.query(query, res);
            return results;
        } catch (SQLException ex) {
            throw ex;
        }
    }
    
    public static List<Row> executeQuery(final String database, final String queryName, final List<StoredProcedureParam> params) throws SQLException {
        l.debug("Executing stored procedure " + database + "." + queryName);
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        final String call = JongoUtils.getCallableStatementCallString(queryName, params.size());
        Connection conn = run.getDataSource().getConnection();
        CallableStatement cs = conn.prepareCall(call);
        final List<StoredProcedureParam> outParams = addParameters(cs, params);
        List<Row> rows = new ArrayList<Row>();
        if(cs.execute()){
            l.debug("Got a result set " + queryName);
            ResultSet rs = cs.getResultSet();
            JongoResultSetHandler handler = new JongoResultSetHandler(true);
            rows = handler.handle(rs);
        }else if(!outParams.isEmpty()){
            l.debug("No result set, but we are expecting OUT values from " + queryName);
            Map<String, String> results = new HashMap<String, String>();
            for(StoredProcedureParam p : outParams){
                results.put(p.getName(), cs.getString(p.getIndex())); // thank $deity we only return strings
            }
            rows.add(new Row(0, results));
        }
        return rows;
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
    
    public static List<Row> getListOfTables(final String database) throws SQLException{
        l.debug("Obtaining the list of tables for the database " + database);
//        if(!conf.allowListTables()){
//            throw JongoJDBCExceptionFactory.getException(database, "Cant read database metadata. Access Denied", JongoJDBCException.ILLEGAL_READ_CODE);
//        }
        ResultSetHandler<List<Row>> res = new JongoResultSetHandler(true);
        DatabaseConfiguration dbconf = conf.getDatabaseConfiguration(database);
        String query = dbconf.getListOfTablesQuery();
        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
        try {
            List<Row> results = run.query(query, res);
            return results;
        } catch (SQLException ex) {
            throw ex;
        }
    }
    
    private static List<StoredProcedureParam> addParameters(final CallableStatement cs, final List<StoredProcedureParam> params) throws SQLException{
        List<StoredProcedureParam> outParams = new ArrayList<StoredProcedureParam>();
        int i = 1;
        for(StoredProcedureParam p : params){
            final Integer sqlType = p.getType();
            if(p.isOutParameter()){
                cs.registerOutParameter(i++, sqlType);
                outParams.add(p);
            }else{
                switch(sqlType){
                    case Types.BIGINT:
                    case Types.INTEGER:
                    case Types.TINYINT:
//                    case Types.NUMERIC:
                        cs.setInt(i++, Integer.valueOf(p.getValue())); break;
                    case Types.DATE:
                        cs.setDate(i++, (Date)JongoUtils.parseValue(p.getValue())); break;
                    case Types.TIME:
                        cs.setTime(i++, (Time)JongoUtils.parseValue(p.getValue())); break;
                    case Types.TIMESTAMP:
                        cs.setTimestamp(i++, (Timestamp)JongoUtils.parseValue(p.getValue())); break;
                    case Types.DECIMAL:
                        cs.setBigDecimal(i++, (BigDecimal)JongoUtils.parseValue(p.getValue())); break;
                    case Types.DOUBLE:
                        cs.setDouble(i++, Double.valueOf(p.getValue())); break;
                    case Types.FLOAT:
                        cs.setLong(i++, Long.valueOf(p.getValue())); break;
                    default:
                        cs.setString(i++, p.getValue()); break;
                }
            }
        }
        return outParams;
    }
}
