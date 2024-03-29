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
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Dialect representation of standard SQL98 & SQL2008 operations.
 * @author Alejandro Ayuso 
 */
public class SQLDialect implements Dialect{

    private static final Logger l = LoggerFactory.getLogger(SQLDialect.class);
    
    @Override
    public String toStatementString(final Insert insert) {
        if(insert.getColumns().isEmpty())
            throw new IllegalArgumentException("An insert query can't be empty");
        
        final StringBuilder b = new StringBuilder("INSERT INTO ");
        b.append(insert.getTable().getName());
        if(!insert.getColumns().isEmpty()){
            b.append(" (");
            b.append(StringUtils.join(insert.getColumns().keySet(), ","));
            b.append(") VALUES (");
            b.append(StringUtils.removeEnd(StringUtils.repeat("?,", insert.getColumns().size()), ","));
            b.append(")");
        }
        l.debug(b.toString());
        return b.toString();
    }

    @Override
    public String toStatementString(final Select select) {
        final StringBuilder b = new StringBuilder("SELECT ");
        
        if(select.getLimitParam() == null){
            if(select.isAllColumns()){
                b.append("t.*");
            }else{
                String cols = StringUtils.join(select.getColumns(), ",");
                b.append("t.").append(cols);
            }
            b.append(" FROM ").append(select.getTable().toString()).append(" t");
            if(!select.isAllRecords()){
                appendWhereClause(b,select);
            }
            if(select.getOrderParam() != null){
                b.append(" ORDER BY t.");
                b.append(select.getOrderParam().getColumn()).append(" ").append(select.getOrderParam().getDirection());
            }
        }else{
            b.append("* FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY ");
            
            if(select.getOrderParam() == null){
                b.append("t.");
                b.append(select.getTable().getPrimaryKey());
            }else{
                b.append("t.");
                b.append(select.getOrderParam().getColumn()).append(" ").append(select.getOrderParam().getDirection());
            }
            
            b.append(" ) AS ROW_NUMBER, ");
            if(select.isAllColumns()){
                b.append("t.*");
            }else{
                String cols = StringUtils.join(select.getColumns(), ",");
                b.append("t.").append(cols);
            }
            b.append(" FROM ").append(select.getTable().toString()).append(" t");
            if(!select.isAllRecords()){
                appendWhereClause(b,select);
            }
            b.append(") WHERE ROW_NUMBER BETWEEN ").append(select.getLimitParam().getStart()).append(" AND ").append(select.getLimitParam().getLimit());
        }
        
        l.debug(b.toString());
        return b.toString();
    }

    @Override
    public String toStatementString(final Update update) {
        if(update.getColumns().isEmpty())
            throw new IllegalArgumentException("An update query can't be empty");
        
        final StringBuilder b = new StringBuilder("UPDATE ");
        b.append(update.getTable().getName()).append(" SET ");

        for(String k : update.getColumns().keySet()){
            b.append(k); b.append("=?,");
        }
        
        b.deleteCharAt(b.length() - 1);
        b.append(" WHERE ").append(update.getTable().getPrimaryKey()).append("=?");
        l.debug(b.toString());
        return b.toString();
    }

    @Override
    public String toStatementString(final Delete delete) {
        final StringBuilder b = new StringBuilder("DELETE FROM ");
        b.append(delete.getTable().getName()).append(" WHERE ").append(delete.getTable().getPrimaryKey()).append("=?");
        l.debug(b.toString());
        return b.toString();
    }

    @Override
    public String toStatementString(final DynamicFinder finder, final LimitParam limit, final OrderParam order) {
        if(finder == null || limit == null || order == null)
            throw new IllegalArgumentException("Invalid argument");
        final StringBuilder b = new StringBuilder(finder.getSql());
        b.append(" ORDER BY ");
        b.append(order.getColumn());
        b.append(" ");
        b.append(order.getDirection());
        b.append(" LIMIT ");
        b.append(limit.getLimit());
        b.append(" OFFSET ");
        b.append(limit.getStart());
        l.debug(b.toString());
        return b.toString();
    }

    @Override
    public String listOfTablesStatement() {
        throw new UnsupportedOperationException("Operation not supported");
    }
    
    protected StringBuilder appendWhereClause(final StringBuilder b, Select select){
        b.append(" WHERE t.")
                .append(select.getParameter().getColumnName())
                .append(" ")
                .append(select.getParameter().getOperator().sql()).append(" ?");
        return b;
    }
    
}
