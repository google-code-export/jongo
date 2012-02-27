package org.jongo.jdbc;

import javax.ws.rs.core.MultivaluedMap;

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

/**
 * Holder for a query parameters.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class QueryParams {
    
    private String database;
    private String table;
    private String id;
    private String idField = "id";
    private MultivaluedMap<String, String> params;
    private LimitParam limit = new LimitParam();
    private OrderParam order = new OrderParam();
    
    public QueryParams(){}

    public QueryParams(String database, String table, String id, String idField, MultivaluedMap<String, String> params, LimitParam limit, OrderParam order) {
        this.database = database;
        this.table = table;
        this.id = id;
        this.idField = idField;
        this.params = params;
        this.limit = limit;
        this.order = order;
    }
    
    public String getIdField() {
        return idField;
    }

    public String getDatabase() {
        return database;
    }

    public String getId() {
        return id;
    }

    public MultivaluedMap<String, String> getParams() {
        return params;
    }

    public String getTable() {
        return table;
    }

    public LimitParam getLimit() {
        return limit;
    }

    public OrderParam getOrder() {
        return order;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public void setLimit(LimitParam limit) {
        this.limit = limit;
    }

    public void setOrder(OrderParam order) {
        this.order = order;
    }

    public void setParams(MultivaluedMap<String, String> params) {
        this.params = params;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
