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
import org.jongo.rest.xstream.RowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JDBCExecutor {

    private static final Logger l = LoggerFactory.getLogger(JDBCExecutor.class);
    
    public static int delete(final String table, final String id) throws SQLException, IllegalAccessException {
        l.debug("Deleting from " + table);
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        
        JongoTable result = getJongoTable(table, run);
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        String query = conn.getDeleteQuery(table, result.getCustomId());
        l.debug(query);
        
        return run.update(query, JongoUtils.parseValue(id));
    }
    
    public static int insert(final String table, MultivaluedMap<String, String> formParams) throws SQLException, IllegalAccessException {
        l.debug("Inserting in " + table);
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        
        JongoTable result = getJongoTable(table, run);
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        
        List<String> params = new ArrayList<String>(formParams.size());
        
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        
        String query = conn.getInsertQuery(table, formParams);
        l.debug(query);
        
        return run.update(query, JongoUtils.parseValues(params));
    }
    
    public static int update(final String table, final String id, MultivaluedMap<String, String> formParams) throws SQLException, IllegalAccessException {
        l.debug("Updating table " + table);
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        
        JongoTable result = getJongoTable(table, run);
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        
        List<String> params = new ArrayList<String>(formParams.size());
        
        for(String k : formParams.keySet()){
            params.add(formParams.getFirst(k));
        }
        params.add(id);
        
        String query = conn.getUpdateQuery(table, result.getCustomId(), formParams);
        l.debug(query);
        
        return run.update(query, JongoUtils.parseValues(params));
    }
    
    public static List<RowResponse> get(final String table, final String id) throws SQLException, IllegalAccessException {
        l.debug("Updating table " + table);
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        
        JongoTable result = getJongoTable(table, run);
        
        String query = "SELECT * FROM " + table + " WHERE " + result.getCustomId() + " = ?";
        l.debug(query);
        
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
        return run.query(query, res, JongoUtils.parseValue(id));
    }
    
    private static JongoTable getJongoTable(final String table, final QueryRunner run) throws SQLException, IllegalAccessException{
        ResultSetHandler<JongoTable> rh = new JongoTableResultSetHandler();
        
        JongoTable result = run.query(JongoTable.GET, rh, table);
        if(result == null){
            l.debug("Table " + table + " is not in JongoTables. Access Denied");
            throw new IllegalAccessException("Access Denied to table " + table);
        }
        
        if(!result.getPermits().isWritable()){
            l.debug("Cant write to table " + table + ". Access Denied");
            throw new IllegalAccessException("Access Denied to table " + table);
        }
        
        if(StringUtils.isEmpty(result.getCustomId())){
            l.debug("Table " + table + " has no customId. Using default (id)");
            result.setCustomId("id");
        }
        return result;
    }

    public static List<RowResponse> find(final String query, Object... params) throws SQLException {
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(false);
        List<RowResponse> results = run.query(query, res, params);
        return results;
    }

    public static List<RowResponse> find(final DynamicFinder query, Object... params) {
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler(query.findAll());
        try {
            List<RowResponse> results = run.query(query.getSql(), res, params);
            return results;
        } catch (SQLException ex) {
            l.error(ex.getMessage());
        }
        return null;
    }
    
    private static int update(final String query, Object... params) throws SQLException {
        l.debug(query + " params: " + JongoUtils.varargToString(params));
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        return run.update(query, params);
    }

    public static List<RowResponse> getTableMetaData(final String table) {
        l.debug("Obtaining metadata from table " + table);
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new ResultSetMetaDataHandler();
        String query = "SELECT * FROM " + table;
        try {
            List<RowResponse> results = run.query(query, res);
            return results;
        } catch (SQLException ex) {
            l.error(ex.getMessage());
        }
        return null;
    }
    
    public static void createJongoTablesAndData() throws SQLException{
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        try{
            getJongoTable("JongoTable", run);
        }catch(IllegalAccessException e){
            l.info("JongoTable Exists. No need to continue");
            return;
        }catch(SQLException e){
            l.info("JongoTable Doesn't Exists. Generating configuration");
        }
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        
        update(conn.getCreateJongoQueryTable());
        update(conn.getCreateJongoTableQuery());
        if(conn instanceof OracleConnection){
            JDBCExecutor.update(conn.getCreateJongoQuerySequence());
            JDBCExecutor.update(conn.getCreateJongoQueryTrigger());
            JDBCExecutor.update(conn.getCreateJongoTableSequence());
            JDBCExecutor.update(conn.getCreateJongoTableTrigger());
        }
        update("INSERT INTO JongoTable (name, customId, permits) VALUES (?,?,?)", "JongoTable", "id", Permission.READWRITE.getValue());
        
        String env = System.getProperty("environment");
        if(env != null && env.equalsIgnoreCase("test")){
            update("CREATE TABLE user (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, name VARCHAR(25), age INTEGER, birthday DATE, lastupdate TIMESTAMP, credit DECIMAL(6,2)) ");
            update("INSERT INTO JongoTable (name, customId, permits) VALUES (?,?,?)", "user", "id", Permission.READWRITE.getValue());
        }
    }
}
