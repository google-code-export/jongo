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
package org.jongo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.jongo.exceptions.JongoBadRequestException;
import org.jongo.jdbc.*;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoResponse;
import org.jongo.rest.xstream.JongoSuccess;
import org.jongo.rest.xstream.RowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class RestController {
    
    private static final Logger l = LoggerFactory.getLogger(RestController.class);
    
    private final String database;
    
    public RestController(String database){
        if(StringUtils.isBlank(database))
            throw new IllegalArgumentException("Database name can't be blank, empty or null");
        this.database = database;
    }
    
    public JongoResponse getDatabaseMetadata(){
        JongoResponse response = null;
        
        List<RowResponse> results = null;
        try {
            results = JDBCExecutor.getListOfTables(database);
        } catch (SQLException ex) {
            l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
            response = new JongoError(database, ex);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            response = new JongoError(database, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if(results == null && response == null){
            response = new JongoError(database, Response.Status.NO_CONTENT);
        }
        
        if(response == null){
            response = new JongoSuccess(database, results);
        }
        
        return response;
    }
    
    public JongoResponse getResourceMetadata(final String table){
        JongoResponse response = null;
        
        List<RowResponse> results = null;
        QueryParams params = new QueryParams();
        params.setDatabase(database);
        params.setTable(table);
        try {
            results = JDBCExecutor.getTableMetaData(params);
        } catch (SQLException ex) {
            l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
            response = new JongoError(database, ex);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            response = new JongoError(database, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if(results == null && response == null){
            response = new JongoError(database, Response.Status.NO_CONTENT);
        }
        
        if(response == null){
            response = new JongoSuccess(database, results);
        }
        
        return response;
    }
    
    public JongoResponse getAllResources(final String table){
        return null;
    }
    
    public JongoResponse getResource(final String table, final String customId, final String id, final LimitParam limit, final OrderParam order){
        l.debug("Geting resource from " + database + "." + table + " with id " + id);
        
        QueryParams params = new QueryParams();
        params.setDatabase(database);
        params.setTable(table);
        params.setIdField(customId);
        params.setId(id);
        params.setLimit(limit);
        params.setOrder(order);
        
        JongoResponse response = null;
        List<RowResponse> results = null;
        try{
            results = JDBCExecutor.get(params);
        } catch (SQLException ex) {
            l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
            response = new JongoError(database, ex);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            response = new JongoError(database, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if(results == null && response == null){
            response = new JongoError(database, Response.Status.NO_CONTENT);
        }
        
        if(response == null){
            response = new JongoSuccess(database, results);
        }
        
        return response;
    }
    
    public JongoResponse insertResource(final String table, final String customId, final String jsonRequest){
        l.debug("Insert new " + database + "." + table + " with JSON values: " + jsonRequest);
        
        QueryParams params = new QueryParams();
        params.setDatabase(database);
        params.setTable(table);
        params.setIdField(customId);
        
        JongoResponse response = null;
        
        try {
            params.setParams(JongoUtils.getParamsFromJSON(jsonRequest));
        } catch (JongoBadRequestException ex){
            l.info("Received a JongoBadRequestException " + ex.getMessage());
            response = new JongoError(database, Response.Status.BAD_REQUEST, ex.getMessage());
        }
        
        if(response == null)
            response = insertResource(params);
        
        return response;
    }
    
    public JongoResponse insertResource(final String table, final String customId, final MultivaluedMap<String, String> formParams){
        l.debug("Insert new " + database + "." + table + " with values: " + formParams);
        
        JongoResponse response = null;
        if(formParams.size() == 0)
            response = new JongoError(table, Response.Status.BAD_REQUEST, "No arguments given");
        
        QueryParams params = new QueryParams();
        params.setDatabase(database);
        params.setTable(table);
        params.setIdField(customId);
        params.setParams(formParams);
        
        if(response == null)
            response = insertResource(params);
        
        return response;
    }
    
    private JongoResponse insertResource(QueryParams params){
        JongoResponse response = null;
        int result = 0;
        try {
            if(response == null)
                result = JDBCExecutor.insert(params);
        } catch (SQLException ex) {
            l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
            response = new JongoError(database, ex);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            response = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if(result == 0 && response == null){
            response = new JongoError(null, Response.Status.NO_CONTENT);
        }

        if(response == null){
            List<RowResponse> results = new ArrayList<RowResponse>();
            results.add(new RowResponse(0));
            response = new JongoSuccess(null, results, Response.Status.CREATED);
        }
        return response;
    }
    
    public JongoResponse updateResource(final String table, final String customId, final String id, final String jsonRequest){
        l.debug("Update record " + id + " in table " + database + "." + table + " with values: " + jsonRequest);
        JongoResponse response = null;
        
        List<RowResponse> results = null;
        QueryParams params = new QueryParams();
        params.setDatabase(database);
        params.setTable(table);
        params.setIdField(customId);
        params.setId(id);
        try {
            params.setParams(JongoUtils.getParamsFromJSON(jsonRequest));
            results = JDBCExecutor.update(params);
        } catch (SQLException ex) {
            l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
            response = new JongoError(database, ex);
        } catch (JongoBadRequestException ex){
            l.info("Received a JongoBadRequestException " + ex.getMessage());
            response = new JongoError(database, Response.Status.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            response = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if(results == null && response == null){
            response =  new JongoError(table, Response.Status.NO_CONTENT);
        }

        if(response == null){
            response = new JongoSuccess(table, results, Response.Status.OK);
        }
        
        return response;
    }
    
    public JongoResponse deleteResource(final String table, final String customId, final String id){
        l.debug("Delete record " + id + " from table " + database + "." + table);
        
        QueryParams params = new QueryParams();
        params.setDatabase(database);
        params.setTable(table);
        params.setIdField(customId);
        params.setId(id);
        
        JongoResponse response = null;
        int result = 0;
        try {
            result = JDBCExecutor.delete(params);
        } catch (SQLException ex) {
            l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
            response = new JongoError(database, ex);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            response = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if(result == 0 && response == null){
            response = new JongoError(table, Response.Status.NO_CONTENT);
        }

        if(response == null){
            List<RowResponse> results = new ArrayList<RowResponse>();
            results.add(new RowResponse(0));
            response = new JongoSuccess(table, results, Response.Status.OK);
        }
        return response;
    }
    
    public JongoResponse findByColumn(final String table, final String col, final String arg, final LimitParam limit, final OrderParam order){
        l.debug("Geting resource from " + database + "." + table + " with " + col + " value " + arg);
        
        List<RowResponse> results = null;
        QueryParams params = new QueryParams();
        params.setDatabase(database);
        params.setTable(table);
        params.setIdField(col);
        params.setLimit(limit);
        params.setOrder(order);
        
        JongoResponse response = null;
        try {
            results = JDBCExecutor.findByColumn(params, JongoUtils.parseValue(arg));
        } catch (SQLException ex) {
            l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
            response = new JongoError(database, ex);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            response = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if((results == null || results.isEmpty()) && response == null ){
            response = new JongoError(table, Response.Status.NOT_FOUND, "No results for " + table + " with " + col + " = " + arg);
        }
        
        if(response == null){
            response = new JongoSuccess(table, results);
        }
        return response;
    }
    
    public JongoResponse findByDynamicFinder(final String table, final String query, final List<String> values, final LimitParam limit, final OrderParam order){
        l.debug("Find resource from " + database + "." + table + " with " + query);
        
        JongoResponse response = null;
        List<RowResponse> results = null;
        if(query == null){
            response = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
        }else{
            if(values.isEmpty()){
                if(values.isEmpty()){
                    try{
                        DynamicFinder df = DynamicFinder.valueOf(table, query);
                        results = JDBCExecutor.find(database, df, limit, order);
                    } catch (SQLException ex) {
                        l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
                        response = new JongoError(database, ex);
                    } catch (JongoBadRequestException ex){
                        l.info("Received a JongoBadRequestException " + ex.getMessage());
                        response = new JongoError(database, Response.Status.BAD_REQUEST, ex.getMessage());
                    } catch (IllegalArgumentException ex){
                        l.info("Received an Unhandled Exception " + ex.getMessage());
                        response = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                    }
                }else{
                    String value = values.get(0);
                    try{
                        DynamicFinder df = DynamicFinder.valueOf(table, query, value);
                        results = JDBCExecutor.find(database, df, limit, order, JongoUtils.parseValue(value));
                    } catch (SQLException ex) {
                        l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
                        response = new JongoError(database, ex);
                    } catch (JongoBadRequestException ex){
                        l.info("Received a JongoBadRequestException " + ex.getMessage());
                        response = new JongoError(database, Response.Status.BAD_REQUEST, ex.getMessage());
                    } catch (IllegalArgumentException ex){
                        l.info("Received an Unhandled Exception " + ex.getMessage());
                        response = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                    }
                }

            }else{
                try{
                    DynamicFinder df = DynamicFinder.valueOf(table, query, values.toArray(new String []{}));
                    results = JDBCExecutor.find(database, df, limit, order, JongoUtils.parseValues(values));
                } catch (SQLException ex) {
                    l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
                    response = new JongoError(database, ex);
                } catch (JongoBadRequestException ex){
                    l.info("Received a JongoBadRequestException " + ex.getMessage());
                    response = new JongoError(database, Response.Status.BAD_REQUEST, ex.getMessage());
                } catch (IllegalArgumentException ex){
                    l.info("Received an Unhandled Exception " + ex.getMessage());
                    response = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                }
            }
            
            if((results == null || results.isEmpty()) && response == null){
                response = new JongoError(table, Response.Status.NOT_FOUND, "No results for " + query);
            }
        }
        if(response == null){
            response =  new JongoSuccess(table, results);
        }
        
        return response;
    }
    
    public JongoResponse executeStoredProcedure(final String query, final List<String> arguments){
        l.debug("Executing Stored Procedure " + query);
        JongoResponse response = null;
        List<RowResponse> results = null;
        try {
            results = JDBCExecutor.executeQuery(database, query, JongoUtils.parseValues(arguments));
        } catch (SQLException ex) {
            l.info("Received a SQLException " + ex.getMessage() + " with code " + ex.getSQLState());
            response = new JongoError(database, ex);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoResponse error = new JongoError(query, Response.Status.INTERNAL_SERVER_ERROR);
            response = new JongoError(query, Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        if((results == null || results.isEmpty()) && response == null){
            JongoResponse error = new JongoError(query, Response.Status.NOT_FOUND, "No results for " + query);
            response = new JongoError(query, Response.Status.NOT_FOUND, "No results for " + query);
        }
        
        if(response == null){
            JongoResponse r = new JongoSuccess(query, results);
            response = new JongoSuccess(query, results);
        }
        return response;
    }
}
