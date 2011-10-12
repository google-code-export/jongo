/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jongo.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;
import org.jongo.rest.xstream.RowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoResultSetHandler implements ResultSetHandler<List<RowResponse>> {
    
    private final boolean all;
    
    private static final Logger l = LoggerFactory.getLogger(JongoResultSetHandler.class);
    
    public JongoResultSetHandler(final boolean all){
        super();
        this.all = all;
    }

    @Override
    public List<RowResponse> handle(ResultSet rs) throws SQLException {
        List<RowResponse> results = new ArrayList<RowResponse>();
        int rowId = 0;
        if(all){
            while (rs.next()) {
                Map<String, String> map = resultSetToMap(rs);
                if(map != null) results.add(new RowResponse(rowId++, map));
            }
        }else{
            rs.next();
            Map<String, String> map = resultSetToMap(rs);
            if(map != null) results.add(new RowResponse(rowId++, map));
        }
        return results;
    }
    
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
    
}
