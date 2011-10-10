package org.jongo.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.jongo.rest.xstream.RowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JDBCExecutor {
    private static final Logger l = LoggerFactory.getLogger(JDBCExecutor.class);

    public static Map<String, String> resultSetToMap(ResultSet resultSet) throws SQLException {
        Map<String, String> map = new HashMap<String, String>();
        int columnCount = resultSet.getMetaData().getColumnCount();
        
        l.debug("Mapping a result set with " + columnCount + " columns to a Map");
        
        if (columnCount < 2) {
            throw new SQLException("resultSetToMap: At least two columns needed for conversion.");
        }
        
        ResultSetMetaData meta = resultSet.getMetaData();
        for(int i = 1; i < columnCount + 1; i++){
            String k = meta.getColumnName(i).toUpperCase();
            String v = resultSet.getString(i);
            l.debug("Mapping column " + k + " with value : " + v);
            map.put(k, v);
        }
        
        return map;
    }

	public static List<RowResponse> query(final String sql) {
        Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
        List<RowResponse> results = new ArrayList<RowResponse>();
		try {
            conn = JDBCConnectionFactory.getConnection();
			st = conn.createStatement();
            l.debug("Executing " + sql);
			rs = st.executeQuery(sql);
            int rowId = 0;
			while (rs.next()) {
                Map<String, String> map = resultSetToMap(rs);
                if(map != null) results.add(new RowResponse(rowId++, map));
			}
		} catch (SQLException e) {
			l.error(e.getLocalizedMessage());
		} finally {
			try { if(rs != null) rs.close(); } catch (Exception e) { l.error(e.getMessage()); }
            try { if(st != null) st.close(); } catch (Exception e) { l.error(e.getMessage()); }
            try { if(conn != null) conn.close(); } catch (Exception e) { l.error(e.getMessage()); }
		}
        l.debug("Got " + results.size() + " results");
		return results;
	}
    
    public static List<RowResponse> queryWithDBUtils(final String query, Object... params){
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        ResultSetHandler<List<RowResponse>> res = new JongoResultSetHandler();
        try {
            List<RowResponse> results = run.query(query, res, params);
            return results;
        } catch (SQLException ex) {
            l.error(ex.getMessage());
        }
        return null;
    }
    
    public static int updateWithDBUtils(final String query, Object... params){
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        try {
            return run.update(query, params);
        } catch (SQLException ex) {
            l.error(ex.getMessage());
            return 0;
        }
    }
	
	public static String update(final String sql) {
        Connection conn = null;
		Statement st = null;
		try {
			conn = JDBCConnectionFactory.getConnection();
			st = conn.createStatement();
			l.debug("Executing " + sql);
			st.executeUpdate(sql);
		} catch (SQLException e) {
			l.error(e.getMessage());
            return e.getMessage();
		} finally {
            try { if(st != null) st.close(); } catch (Exception e) { l.error(e.getMessage()); }
            try { if(conn != null) conn.close(); } catch (Exception e) { l.error(e.getMessage()); }
		}
        return null;
	}
}
