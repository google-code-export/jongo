package org.jongo.jdbc;

import java.sql.SQLException;
import java.util.List;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.jongo.JongoUtils;
import org.jongo.rest.xstream.RowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JDBCExecutor {

    private static final Logger l = LoggerFactory.getLogger(JDBCExecutor.class);

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
