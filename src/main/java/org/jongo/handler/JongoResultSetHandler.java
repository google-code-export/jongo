/**
 * Copyright (C) 2011, 2012 Alejandro Ayuso
 *
 * This file is part of Jongo.
 * Jongo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * Jongo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jongo.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jongo.handler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jongo.rest.xstream.RowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a ResultSet and convert it to a List of RowResponse
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoResultSetHandler implements ResultSetHandler<List<RowResponse>> {
    
    private final boolean all;
    
    private static final Logger l = LoggerFactory.getLogger(JongoResultSetHandler.class);
    private static final DateTimeFormatter dateTimeFTR = ISODateTimeFormat.dateTime();
    private static final DateTimeFormatter dateFTR = ISODateTimeFormat.date();
    private static final DateTimeFormatter timeFTR = ISODateTimeFormat.time();
    
    public JongoResultSetHandler(final boolean all){
        super();
        this.all = all;
    }

    /**
     * Method in charge of the conversion
     * @param rs the ResultSet
     * @return a List of RowResponse
     * @throws SQLException
     */
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
    
    /**
     * Converts a ResultSet to a Map
     * @param resultSet a ResultSet
     * @return a Map with the column names as keys and the values. null if something goes wrong.
     */
    public static Map<String, String> resultSetToMap(ResultSet resultSet) {
        Map<String, String> map = new HashMap<String, String>();
        try{
            int columnCount = resultSet.getMetaData().getColumnCount();

            l.trace("Mapping a result set with " + columnCount + " columns to a Map");

            ResultSetMetaData meta = resultSet.getMetaData();
            for(int i = 1; i < columnCount + 1; i++){
                String colName = meta.getColumnName(i).toUpperCase();
                int colType = meta.getColumnType(i);
                String v = resultSet.getString(i);
                if(colType == Types.DATE){
                    v = new DateTime(resultSet.getDate(i)).toString(dateFTR);
                    l.trace("Mapped DATE column " + colName + " with value : " + v);
                }else if(colType == Types.TIMESTAMP){
                    v = new DateTime(resultSet.getTimestamp(i)).toString(dateTimeFTR);
                    l.trace("Mapped TIMESTAMP column " + colName + " with value : " + v);
                }else if(colType == Types.TIME){
                    v = new DateTime(resultSet.getTimestamp(i)).toString(timeFTR);
                    l.trace("Mapped TIME column " + colName + " with value : " + v);
                }else{
                    l.trace("Mapped " + meta.getColumnTypeName(i) + " column " + colName + " with value : " + v);
                }
                map.put(colName, v);
            }
        }catch(SQLException e){
            l.error("Failed to map ResultSet");
            l.error(e.getMessage());
            return null;
        }
        
        return map;
    }
    
}
