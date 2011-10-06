package org.jongo.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.jongo.rest.xstream.RowResponse;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface JongoJDBCConnection {
    
    public Connection getConnection();
    
    public Statement getStatement() throws SQLException;
    
    public void close();
    
    public List<RowResponse> query(final String sql);
    
    public void update(final String sql) throws SQLException;
    
    public Map<String, String> resultSetToMap(ResultSet resultSet) throws SQLException;
    
}
