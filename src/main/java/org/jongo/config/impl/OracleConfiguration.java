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
package org.jongo.config.impl;

import org.apache.commons.lang.StringUtils;
import org.jongo.config.AbstractDatabaseConfiguration;
import org.jongo.config.DatabaseConfiguration;
import org.jongo.enums.JDBCDriver;
import org.jongo.jdbc.DynamicFinder;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oracle 8g and later DatabaseConfiguration implementation.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class OracleConfiguration extends AbstractDatabaseConfiguration implements DatabaseConfiguration {
    
    private static final Logger l = LoggerFactory.getLogger(OracleConfiguration.class);
    
    private static boolean loaded = false;
    
    public OracleConfiguration(String name, String user, String password, String url){
        this.name = name;
        this.driver = JDBCDriver.ORACLE;
        this.username = user;
        this.password = password;
        this.url = url;
    }
    
    @Override
    public void loadDriver() {
        if(!loaded){
            l.debug("Loading Oracle Driver " + this.driver.getName());
            try {
                Class.forName(this.driver.getName());
                loaded = true;
            } catch (ClassNotFoundException ex) {
                l.error("Unable to load driver. Add the Oracle JDBC Connector jar to the lib folder");
            }
        }
    }

    @Override
    public String getFirstRowQuery(String table) {
        if(StringUtils.isBlank(table))
            throw new IllegalArgumentException("Table name can't be blank, empty or null");
        return "SELECT * FROM " + table + " WHERE rownum = 0";
    }
    
    @Override
    public String getSelectAllFromTableQuery(final String table, LimitParam limit, OrderParam order){
        if(StringUtils.isBlank(table) || limit == null || order == null)
            throw new IllegalArgumentException("Invalid argument");
        
        final StringBuilder query = new StringBuilder("SELECT * FROM ( SELECT ROW_NUMBER() OVER (ORDER BY ");
        query.append(order.getColumn());
        query.append(" ");
        query.append(order.getDirection());
        query.append(" )AS ROW_NUMBER, ");
        query.append(table);
        query.append(".* FROM ");
        query.append(table);
        query.append(" ) k WHERE ROW_NUMBER BETWEEN ");
        query.append(limit.getLimit());
        query.append(" AND ");
        query.append(limit.getStart());
        return query.toString();
    }
    
    @Override
    public String getSelectAllFromTableQuery(final String table, final String idCol, LimitParam limit, OrderParam order){
        if(StringUtils.isBlank(table) || StringUtils.isBlank(idCol) || limit == null || order == null)
            throw new IllegalArgumentException("Invalid argument");
        
        final StringBuilder query = new StringBuilder("SELECT * FROM ( SELECT ROW_NUMBER() OVER (ORDER BY ");
        query.append(order.getColumn());
        query.append(" ");
        query.append(order.getDirection());
        query.append(" )AS ROW_NUMBER, ");
        query.append(table);
        query.append(".* FROM ");
        query.append(table);
        query.append(" WHERE ");
        query.append(idCol);
        
        query.append("= ? ) k WHERE ROW_NUMBER BETWEEN ");
        query.append(limit.getLimit());
        query.append(" AND ");
        query.append(limit.getStart());
        return query.toString();
    }

    @Override
    public String getListOfTablesQuery() {
        return "SELECT TABLE_NAME FROM ALL_ALL_TABLES";
    }
    
    @Override
    public String wrapDynamicFinderQuery(final DynamicFinder finder, LimitParam limit, OrderParam order){
        if(finder == null || limit == null || order == null)
            throw new IllegalArgumentException("Invalid argument");
        
        final String [] parts = finder.getSql().split("WHERE");
        final StringBuilder query = new StringBuilder("SELECT * FROM ( SELECT ROW_NUMBER() OVER (ORDER BY ");
        query.append(order.getColumn());
        query.append(" ");
        query.append(order.getDirection());
        query.append(" )AS ROW_NUMBER, ");
        query.append(finder.getTable());
        query.append(".* FROM ");
        query.append(finder.getTable());
        query.append(" WHERE ");
        query.append(parts[1]);
        query.append(" ) k WHERE ROW_NUMBER BETWEEN ");
        query.append(limit.getLimit());
        query.append(" AND ");
        query.append(limit.getStart());
        return query.toString();
    }
    
    @Override
    public boolean isValid() {
        if(super.isValid()){
            if(!StringUtils.startsWith(url, "jdbc:oracle:thin:")){
                l.warn("Invalid JDBC URL. Check your configuration.");
                return false;
            }
            return true;
        }else{
            return false;
        }
    }
    
}
