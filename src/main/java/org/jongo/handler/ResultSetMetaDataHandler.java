package org.jongo.handler;

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
public class ResultSetMetaDataHandler implements ResultSetHandler<List<RowResponse>> {

    private static final Logger l = LoggerFactory.getLogger(ResultSetMetaDataHandler.class);

    @Override
    public List<RowResponse> handle(ResultSet rs) throws SQLException {
        List<RowResponse> results = new ArrayList<RowResponse>();
        int rowId = 0;
        ResultSetMetaData metaData = rs.getMetaData();
        Map<String,String> map = null;
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            map = new HashMap<String,String>(2);
            map.put("tableName", metaData.getTableName(i));
            map.put("columnName", metaData.getColumnName(i));
            map.put("columnLabel", metaData.getColumnLabel(i));
            map.put("columnType", metaData.getColumnTypeName(i));
            map.put("columnSize", String.valueOf(metaData.getColumnDisplaySize(i)));
            map.put("precision", String.valueOf(metaData.getPrecision(i)));
            map.put("scale", String.valueOf(metaData.getScale(i)));
            
//            map.put("catalog_name", metaData.getCatalogName(i));
//            map.put("column_class_name", metaData.getColumnClassName(i));
//            map.put("schema_name", metaData.getSchemaName(i));
//            map.put("column_type", String.valueOf(metaData.getColumnType(i)));
 
            if(map != null) results.add(new RowResponse(rowId++, map));
        }
        return results;
    }
}
