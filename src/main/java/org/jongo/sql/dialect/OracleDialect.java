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
package org.jongo.sql.dialect;

import org.apache.commons.lang.StringUtils;
import org.jongo.jdbc.DynamicFinder;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.sql.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class OracleDialect extends SQLDialect {
    
    private static final Logger l = LoggerFactory.getLogger(OracleDialect.class);

    @Override
    public String listOfTablesStatement() {
        return "SELECT TABLE_NAME FROM ALL_ALL_TABLES";
    }

    @Override
    public String toStatementString(Select select) {
        
        // SELECT a_table.* FROM demo1.a_table WHERE a_table.tableId=? ORDER BY a_table.tableId ASC
        // SELECT * FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY a_table.tableId ASC ) AS ROW_NUMBER, a_table.* FROM demo1.a_table ) WHERE ROW_NUMBER BETWEEN 0 AND 25
        final StringBuilder b = new StringBuilder("SELECT ");
        
        if(select.getLimitParam() == null){
            if(select.isAllColumns()){
                b.append(select.getTable().getName()).append(".*");
            }else{
                String cols = StringUtils.join(select.getColumns(), ",");
                b.append(cols);
            }
            b.append(" FROM ").append(select.getTable().toString());
            if(!select.isAllRecords()){
                b.append(" WHERE ");
                if(StringUtils.isEmpty(select.getColumn())){
                    b.append(select.getTable().getName()).append(".").append(select.getTable().getPrimaryKey()).append("=?");
                }else{
                    b.append(select.getTable().getName()).append(".").append(select.getColumn()).append("=?");
                }
            }
            if(select.getOrderParam() != null){
                b.append(" ORDER BY ").append(select.getTable().getName()).append(".");
                b.append(select.getOrderParam().getColumn()).append(" ").append(select.getOrderParam().getDirection());
            }
        }else{
            b.append("* FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY ");
            
            if(select.getOrderParam() == null){
                b.append(select.getTable().getName()).append(".");
                b.append(select.getTable().getPrimaryKey());
            }else{
                b.append(select.getTable().getName()).append(".");
                b.append(select.getOrderParam().getColumn()).append(" ").append(select.getOrderParam().getDirection());
            }
            
            b.append(" ) AS ROW_NUMBER, ");
            if(select.isAllColumns()){
                b.append(select.getTable().getName()).append(".*");
            }else{
                String cols = StringUtils.join(select.getColumns(), ",");
                b.append(cols);
            }
            b.append(" FROM ").append(select.getTable().toString());
            if(!select.isAllRecords()){
                b.append(" WHERE ");
                if(StringUtils.isEmpty(select.getColumn())){
                    b.append(select.getTable().getName()).append(".").append(select.getTable().getPrimaryKey()).append("=?");
                }else{
                    b.append(select.getTable().getName()).append(".").append(select.getColumn()).append("=?");
                }
            }
            b.append(") WHERE ROW_NUMBER BETWEEN ").append(select.getLimitParam().getStart()).append(" AND ").append(select.getLimitParam().getLimit());
        }
        
        l.debug(b.toString());
        return b.toString();
    }

    @Override
    public String toStatementString(DynamicFinder finder, LimitParam limit, OrderParam order) {
        if(finder == null || limit == null || order == null)
            throw new IllegalArgumentException("Invalid argument");
        
        final String [] parts = finder.getSql().split("WHERE");
        final StringBuilder query = new StringBuilder("SELECT * FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY ");
        query.append(finder.getTable()).append(".").append(order.getColumn());
        query.append(" ");
        query.append(order.getDirection());
        query.append(" ) AS ROW_NUMBER, ");
        query.append(finder.getTable());
        query.append(".* FROM ");
        query.append(finder.getTable());
        query.append(" WHERE ");
        query.append(parts[1]);
        query.append(" ) WHERE ROW_NUMBER BETWEEN ");
        query.append(limit.getStart());
        query.append(" AND ");
        query.append(limit.getLimit());
        return query.toString();
    }
}
