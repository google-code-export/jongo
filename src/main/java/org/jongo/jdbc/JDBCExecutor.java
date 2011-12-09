package org.jongo.jdbc;

import java.util.logging.Level;
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
import org.jongo.JongoConfiguration;
import org.jongo.JongoUtils;
import org.jongo.demo.Demo;
import org.jongo.domain.JongoQuery;
import org.jongo.domain.JongoTable;
import org.jongo.handler.JongoQueryResultSetHandler;
import org.jongo.handler.JongoTableResultSetHandler;
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
    private static final JongoConfiguration configuration = JongoConfiguration.instanceOf();
    
    public static int delete(final String table, final String id) throws JongoJDBCException {
        l.debug("Deleting from " + table);
        
        JongoTable result = isWritable(table);
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        String query = conn.getDeleteQuery(table, result.getCustomId());
        l.debug(query);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        try {
            return run.update(query, JongoUtils.parseValue(id));
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static int insert(final String table, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        JongoTable jongoTable = isWritable(table);
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        
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
        
        String query = conn.getInsertQuery(table, formParams);
        l.debug(query);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        try {
            return run.update(query, JongoUtils.parseValues(params));
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> update(final String table, final String id, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        l.debug("Updating table " + table);
        
        JongoTable result = isWritable(table);
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        
        List<String> params = new ArrayList<String>(formParams.size());
        
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        params.add(id);
        
        String query = conn.getUpdateQuery(table, result.getCustomId(), formParams);
        l.debug(query);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        List<RowResponse> results = null;
        try {
            int ret = run.update(query, JongoUtils.parseValues(params));
            if(ret != 0){
                results = get(table, id);
            }
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
        return results;
    }
    
    public static List<RowResponse> get(final String table, final String id) throws JongoJDBCException {
        JongoTable result = isReadable(table);
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        
        List<RowResponse> response = null;
        
        if(StringUtils.isBlank(id)){
            String query = conn.getSelectAllFromTableQuery(table);
            l.debug(query);
        
            ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
            try {
                response = run.query(query, res);
            } catch (SQLException ex) {
                throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
            }
        }else{
            String query = conn.getSelectAllFromTableQuery(table, result.getCustomId());
            l.debug(query);
        
            ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
            try {
                response =  run.query(query, res, JongoUtils.parseValue(id));
            } catch (SQLException ex) {
                throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
            }
        }
        return response;
        
    }
    
    private static JongoTable isWritable(final String table) throws JongoJDBCException{
        l.debug("Checking if table is writable " + table );
        ResultSetHandler<JongoTable> rh = new JongoTableResultSetHandler();
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        JongoTable result = null;
        try {
            result = run.query(JongoTable.GET, rh, table);
        } catch (SQLException ex) {
            l.debug(ex.getMessage());
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }

        if(result == null){
            l.debug("Table " + table + " is not in JongoTables. Access Denied");
            throw JongoJDBCExceptionFactory.getException("Table " + table + " is not in JongoTables. Access Denied", JongoJDBCException.ILLEGAL_ACCESS_CODE);
        }
        
        if(!result.getPermits().isWritable()){
            l.debug("Table " + table + " is not writable. Access Denied");
            throw JongoJDBCExceptionFactory.getException("Cant write to table " + table + ". Access Denied", JongoJDBCException.ILLEGAL_WRITE_CODE);
        }
        
        if(StringUtils.isEmpty(result.getCustomId())){
            l.debug("Table " + table + " has no customId. Using default (id)");
            result.setCustomId("id");
        }
        return result;
    }
    
    private static JongoTable isReadable(final String table) throws JongoJDBCException{
        l.debug("Checking if table is readable " + table );
        ResultSetHandler<JongoTable> rh = new JongoTableResultSetHandler();
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        JongoTable result = null;
        try {
            result = run.query(JongoTable.GET, rh, table);
        } catch (SQLException ex) {
            l.debug(ex.getMessage());
        }

        if(result == null){
            l.debug("Table " + table + " is not in JongoTables. Access Denied");
            throw JongoJDBCExceptionFactory.getException("Table " + table + " is not in JongoTables. Access Denied", JongoJDBCException.ILLEGAL_ACCESS_CODE);
        }
        
        if(!result.getPermits().isReadable()){
            l.debug("Table " + table + " is not readable. Access Denied");
            throw JongoJDBCExceptionFactory.getException("Cant read table " + table + ". Access Denied", JongoJDBCException.ILLEGAL_READ_CODE);
        }
        
        if(StringUtils.isEmpty(result.getCustomId())){
            l.debug("Table " + table + " has no customId. Using default (id)");
            result.setCustomId("id");
        }
        return result;
    }

    public static List<RowResponse> findByColumn(final String table, final String column, Object... params) throws JongoJDBCException {
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        String query = conn.getSelectAllFromTableQuery(table, column);
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        
        isReadable(table);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
        try {
            List<RowResponse> results = run.query(query, res, params);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> find(final DynamicFinder query, Object... params) throws JongoJDBCException{
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        
        isReadable(query.getTable());
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(query.findAll());
        try {
            List<RowResponse> results = run.query(query.getSql(), res, params);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    private static int update(final QueryRunner run, final String query, final Object... params) throws SQLException {
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        return run.update(query, params);
    }

    public static List<RowResponse> getTableMetaData(final String table) throws JongoJDBCException {
        l.debug("Obtaining metadata from table " + table);
        
        isReadable(table);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new ResultSetMetaDataHandler();
        String query = JDBCConnectionFactory.getJongoJDBCConnection().getFirstRowQuery(table);
        try {
            List<RowResponse> results = run.query(query, res);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> executeQuery(final String queryName, final Object... params) throws JongoJDBCException {
        l.debug("Executing query " + queryName + " params: " + JongoUtils.varargToString(params));
        
        JongoQuery query = getJongoQuery(queryName);
        if(query == null){
            return null;
        }
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        try {
            List<RowResponse> results = run.query(query.getCleanQuery(), res, params);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    private static JongoQuery getJongoQuery(final String name) throws JongoJDBCException{
        ResultSetHandler<JongoQuery> rh = new JongoQueryResultSetHandler();
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        JongoQuery result = null;
        try {
            result = run.query(JongoQuery.GET, rh, name);
        } catch (SQLException ex) {
            l.debug(ex.getMessage());
        }
        
        return result;
    }
    
    public static void createJongoTablesAndData() throws SQLException{
        QueryRunner adminRun = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
        String query = "SELECT * FROM JongoTable";
        List<RowResponse> results = null;
        try {
            results = adminRun.query(query, res);
        } catch (SQLException ex) {}
        
        if(results != null){
            l.info("No need to create admin tables");
        }else{
            l.info("Creating Jongo Tables");
            update(adminRun, JDBCConnectionFactory.createJongoTableQuery);
            update(adminRun, JDBCConnectionFactory.createJongoQueryTableQuery);
        }
        
        if(configuration.isDemoModeActive()){
            Demo.generateDemoDatabase();
        }
    }
    
    public static int adminInsert(final String table, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        l.debug("Inserting in admin " + table);
        
        List<String> params = new ArrayList<String>(formParams.size());
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        String query = conn.getInsertQuery(table, formParams);
        l.debug(query);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        try {
            return run.update(query, JongoUtils.parseValues(params));
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> adminFind(final String table, final String query, Object... params) throws JongoJDBCException {
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(true);
        try {
            List<RowResponse> results = run.query(query, res, params);
            return results;
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static int adminUpdate(final String table, final String id, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        l.debug("Updating admin table " + table);
        
        List<String> params = new ArrayList<String>(formParams.size());
        
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        params.add(id);
        
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        String query = conn.getUpdateQuery(table, "id", formParams);
        l.debug(query);
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        try {
            return run.update(query, JongoUtils.parseValues(params));
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static int adminDelete(final String table, final String id) throws JongoJDBCException {
        l.debug("Deleting admin " + table);
        
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        String query = conn.getDeleteQuery(table, "id");
        l.debug(query);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        try {
            return run.update(query, JongoUtils.parseValue(id));
        } catch (SQLException ex) {
            throw JongoJDBCExceptionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static void shutdown(){
        l.debug("Shutting down JDBC connections");
        try {
            JDBCConnectionFactory.getDataSource().getConnection().close();
            JDBCConnectionFactory.getAdminDataSource().getConnection().close();
        } catch (SQLException ex) {
            l.warn("Failed to close connection to database?");
            l.debug(ex.getMessage());
        }
    }
}
