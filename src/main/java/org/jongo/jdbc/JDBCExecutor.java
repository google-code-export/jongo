package org.jongo.jdbc;

import org.jongo.handler.ResultSetMetaDataHandler;
import org.jongo.handler.JongoResultSetHandler;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.jongo.JongoUtils;
import org.jongo.domain.JongoTable;
import org.jongo.handler.JongoTableResultSetHandler;
import org.jongo.rest.xstream.RowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JDBCExecutor {

    private static final Logger l = LoggerFactory.getLogger(JDBCExecutor.class);
    
    public static final int NONE = 0;
    public static final int READ = 1;
    public static final int WRITE = 2;
    public static final int RW = 3;
    
    
    public static int delete(final String table, final String id) throws SQLException, IllegalAccessException {
        l.debug("Deleting from " + table);
        ResultSetHandler<JongoTable> rh = new JongoTableResultSetHandler();
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        
        JongoTable result = run.query(JongoTable.GET, rh, table);
        if(result == null){
            l.debug("Table " + table + " is not in JongoTables. Access Denied");
            throw new IllegalAccessException("Access Denied to table " + table);
        }
        
        if(result.getPermits() == NONE || result.getPermits() == 1){
            l.debug("Cant write to table " + table + ". Access Denied");
            throw new IllegalAccessException("Access Denied to table " + table);
        }
        
        if(StringUtils.isEmpty(result.getCustomId())){
            l.debug("Table " + table + " has no customId. Using default (id)");
            result.setCustomId("id");
        }
        
        JongoJDBCConnection conn = JDBCConnectionFactory.getJongoJDBCConnection();
        String query = conn.getDeleteQuery(table, result.getCustomId());
        l.debug(query);
        
        return run.update(query, JongoUtils.parseValue(id));
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
    
    public static int update(final String query, Object... params) throws SQLException {
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
}
