package org.jongo.jdbc;

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
import org.jongo.domain.JongoTable;
import org.jongo.enums.Permission;
import org.jongo.handler.JongoTableResultSetHandler;
import org.jongo.jdbc.connections.OracleConnection;
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
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static int insert(final String table, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        l.debug("Inserting in " + table);
        
        isWritable(table);
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        
        List<String> params = new ArrayList<String>(formParams.size());
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        
        String query = conn.getInsertQuery(table, formParams);
        l.debug(query);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        try {
            return run.update(query, JongoUtils.parseValues(params));
        } catch (SQLException ex) {
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static int update(final String table, final String id, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
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
        try {
            return run.update(query, JongoUtils.parseValues(params));
        } catch (SQLException ex) {
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    public static List<RowResponse> get(final String table, final String id) throws JongoJDBCException {
        l.debug("Getting table " + table);
        
        JongoTable result = isReadable(table);
        
        String query = "SELECT * FROM " + table + " WHERE " + result.getCustomId() + " = ?";
        l.debug(query);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
        try {
            return run.query(query, res, JongoUtils.parseValue(id));
        } catch (SQLException ex) {
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    private static JongoTable isWritable(final String table) throws JongoJDBCException{
        ResultSetHandler<JongoTable> rh = new JongoTableResultSetHandler();
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        JongoTable result = null;
        try {
            result = run.query(JongoTable.GET, rh, table);
        } catch (SQLException ex) {
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
        }

        if(result == null){
            throw JDBCConnectionFactory.getException("Table " + table + " is not in JongoTables. Access Denied", JongoJDBCException.ILLEGAL_ACCESS_CODE);
        }
        
        if(!result.getPermits().isWritable()){
            throw JDBCConnectionFactory.getException("Cant write to table " + table + ". Access Denied", JongoJDBCException.ILLEGAL_WRITE_CODE);
        }
        
        if(StringUtils.isEmpty(result.getCustomId())){
            l.debug("Table " + table + " has no customId. Using default (id)");
            result.setCustomId("id");
        }
        return result;
    }
    
    private static JongoTable isReadable(final String table) throws JongoJDBCException{
        ResultSetHandler<JongoTable> rh = new JongoTableResultSetHandler();
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        JongoTable result = null;
        try {
            result = run.query(JongoTable.GET, rh, table);
        } catch (SQLException ex) {
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
        }

        if(result == null){
            throw JDBCConnectionFactory.getException("Table " + table + " is not in JongoTables. Access Denied", JongoJDBCException.ILLEGAL_ACCESS_CODE);
        }
        
        if(!result.getPermits().isReadable()){
            throw JDBCConnectionFactory.getException("Cant read table " + table + ". Access Denied", JongoJDBCException.ILLEGAL_READ_CODE);
        }
        
        if(StringUtils.isEmpty(result.getCustomId())){
            l.debug("Table " + table + " has no customId. Using default (id)");
            result.setCustomId("id");
        }
        return result;
    }

    public static List<RowResponse> find(final String table, final String query, Object... params) throws JongoJDBCException {
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        
        isReadable(table);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
        try {
            List<RowResponse> results = run.query(query, res, params);
            return results;
        } catch (SQLException ex) {
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
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
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
        }
    }
    
    private static int update(final QueryRunner run, final String query, Object... params) throws SQLException {
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        return run.update(query, params);
    }

    public static List<RowResponse> getTableMetaData(final String table) throws JongoJDBCException {
        l.debug("Obtaining metadata from table " + table);
        
        isReadable(table);
        
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new ResultSetMetaDataHandler();
        String query = "SELECT * FROM " + table;
        try {
            List<RowResponse> results = run.query(query, res);
            return results;
        } catch (SQLException ex) {
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
        }
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
            return;
        }
        l.info("Creating Jongo Tables");
        update(adminRun, JDBCConnectionFactory.createJongoTableQuery);
        update(adminRun, JDBCConnectionFactory.createJongoQueryTableQuery);
        
        String env = System.getProperty("environment");
        if(env != null && env.equalsIgnoreCase("test")){
            QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
            update(run, "CREATE TABLE user (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, name VARCHAR(25), age INTEGER, birthday DATE, lastupdate TIMESTAMP, credit DECIMAL(6,2)) ");
            update(adminRun, "INSERT INTO JongoTable (name, customId, permits) VALUES (?,?,?)", "user", "id", Permission.READWRITE.getValue());
            update(adminRun, "INSERT INTO JongoQuery (name, query) VALUES (?,?)", "userQuery", "SELECT * FROM user WHERE id > 0 GROUP BY name, age LEFT JOIN foo WHERE foo id = 132");
        }
    }
    
    public static int adminInsert(final String table, MultivaluedMap<String, String> formParams) throws JongoJDBCException {
        l.debug("Inserting in " + table);
        
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
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
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
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
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
            throw JDBCConnectionFactory.getException(ex.getMessage(), ex);
        }
    }
}
