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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MSSQL DatabaseConfiguration implementation. Needs to be implemented. Volunteers?
 */
public class MSSQLConfiguration  extends AbstractDatabaseConfiguration implements DatabaseConfiguration  {

    private static final Logger l = LoggerFactory.getLogger(MSSQLConfiguration.class);
    
    public MSSQLConfiguration(String name, String user, String password, String url){
        this.name = name;
        this.driver = JDBCDriver.MSSQL;
        this.username = user;
        this.password = password;
        this.url = url;
    }
    
    @Override
    public String getListOfTablesQuery() {
        return "select * from information_schema.tables where Table_Type = 'BASE TABLE'";
    }
    
    /**
     * MSSQL doesn't support the standard way as described in http://en.wikipedia.org/wiki/Select_(SQL)#FETCH_FIRST_clause
     * @param table
     * @return a MSSQL query that when executed should only return the first row of a table.
     */
    @Override
    public String getFirstRowQuery(final String table) {
        if(StringUtils.isBlank(table))
            throw new IllegalArgumentException("Table name can't be blank, empty or null");
        return "SELECT TOP 1 * FROM " + table;
    }
    
}
