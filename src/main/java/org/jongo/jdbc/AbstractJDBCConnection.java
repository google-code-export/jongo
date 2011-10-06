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
import org.jongo.enums.JDBCDriver;
import org.jongo.rest.xstream.RowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class AbstractJDBCConnection {
    
    protected String url;
    protected String username;
    protected String password;
    protected JDBCDriver driver;
    
    private static final Logger l = LoggerFactory.getLogger(AbstractJDBCConnection.class);
    protected Connection conn;
    
    public Map<String, String> resultSetToMap(ResultSet resultSet) throws SQLException {
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
    
    public Statement getStatement() throws SQLException {
		return conn.createStatement();
	}

	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			l.error(e.getMessage(), e);
		}
	}

	public List<RowResponse> query(final String sql) {
		Statement st = null;
		ResultSet rs = null;
        List<RowResponse> results = new ArrayList<RowResponse>();
		try {
			st = getStatement();
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
			try {
				rs.close();
				st.close();
			} catch (SQLException e) {
				l.error(e.getLocalizedMessage());
			}
		}
        l.debug("Got " + results.size() + " results");
		return results;
	}
	
	public void update(final String sql) {
		Statement st = null;
		try {
			st = getStatement();
			l.debug("Executing " + sql);
			st.executeUpdate(sql);
		} catch (SQLException e) {
			l.error(e.getLocalizedMessage());
		} finally {
			try {
				st.close();
			} catch (SQLException e) {
				l.error(e.getLocalizedMessage());
			}
		}
	}
}
