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
import java.util.Map;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.jongo.config.JongoConfiguration;
import org.jongo.exceptions.JongoBadRequestException;
import org.jongo.jdbc.*;
import org.jongo.rest.xstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class RestController {
    
    private static final Logger l = LoggerFactory.getLogger(RestController.class);
    private static final JongoConfiguration conf = JongoConfiguration.instanceOf();
    
    private final String database;
    
    public RestController(String database){
        if(StringUtils.isBlank(database))
            throw new IllegalArgumentException("Database name can't be blank, empty or null");
        this.database = database;
    }
    
    public JongoResponse getDatabaseMetadata(){
        l.debug("Obtaining metadata for " + database);
        JongoResponse response = null;
        
        if(!conf.getDatabases().contains(database))
            return new JongoError(database, Response.Status.NOT_FOUND, "Database doesn't exists or is not registered in jongo");
        
        List<Row> results = null;
        try {
            results = JDBCExecutor.getListOfTables(database);
        } catch (Throwable ex){
            response = handleException(ex, database);
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
        l.debug("Obtaining metadata for " + table);
        
        QueryParams params;
        try{
            params = QueryParams.valueOf(database, table);
        }catch (IllegalArgumentException e){
            l.debug("Failed to generate query params " + e.getMessage());
            return new JongoError(table, Response.Status.BAD_REQUEST, e.getMessage());
        }
        
        JongoResponse response = null;
        List<Row> results = null;
        try {
            results = JDBCExecutor.getTableMetaData(params);
        } catch (Throwable ex){
            response = handleException(ex, table);
        }
        
        if(results == null && response == null){
            response = new JongoError(table, Response.Status.NO_CONTENT);
        }
        
        if(response == null){
            response = new JongoHead(table, results);
        }
        
        return response;
    }
    
    /**
     * Retrieves all resources from a given table ordered and limited.
     * @param table the table or view to query
     * @param limit a LimitParam object with the limit values
     * @param order order an OrderParam object with the ordering values.
     * @return Returns a JongoResponse with the values of the resource. If the resource is not available an error
     * if the table is empty, we return a SuccessResponse with no values.
     */
    public JongoResponse getAllResources(final String table, final LimitParam limit, final OrderParam order){
        l.debug("Geting all resources from " + database + "." + table);
        
        QueryParams params;
        try{
            params = QueryParams.valueOf(database, table, null, null, limit, order);
        }catch (IllegalArgumentException e){
            l.debug("Failed to generate query params " + e.getMessage());
            return new JongoError(table, Response.Status.BAD_REQUEST, e.getMessage());
        }
        
        JongoResponse response = null;
        List<Row> results = null;
        try{
            results = JDBCExecutor.get(params);
        } catch (Throwable ex){
            response = handleException(ex, table);
        }
        
        if(results == null && response == null){
            response = new JongoError(table, Response.Status.NOT_FOUND);
        }
        
        if(response == null){
            response = new JongoSuccess(table, results);
        }
        
        return response;
    }
    
    /**
     * Retrieves one resource for the given id. 
     * @param table the table or view to query
     * @param customId the column defined to be the primary key. Defaults to "id"
     * @param id the identification value of the resource.
     * @param limit a LimitParam object with the limit values
     * @param order an OrderParam object with the ordering values.
     * @return Returns a JongoResponse with the values of the resource. If the resource is not available an error is returned.
     */
    public JongoResponse getResource(final String table, final String customId, final String id, final LimitParam limit, final OrderParam order){
        l.debug("Geting resource from " + database + "." + table + " with id " + id);
        
        QueryParams params;
        try{
            params = QueryParams.valueOf(database, table, id, customId, limit, order);
        }catch (IllegalArgumentException e){
            l.debug("Failed to generate query params " + e.getMessage());
            return new JongoError(table, Response.Status.BAD_REQUEST, e.getMessage());
        }
        
        JongoResponse response = null;
        List<Row> results = null;
        try{
            results = JDBCExecutor.get(params);
        } catch (Throwable ex){
            response = handleException(ex, table);
        }
        
        if((results == null || results.isEmpty()) && response == null){
            response = new JongoError(table, Response.Status.NOT_FOUND);
        }
        
        if(response == null){
            response = new JongoSuccess(table, results);
        }
        
        return response;
    }
    
    public JongoResponse insertResource(final String table, final String customId, final String jsonRequest){
        l.debug("Insert new " + database + "." + table + " with JSON values: " + jsonRequest);
        
        JongoResponse response;
        
        try {
            Map<String, String> params = JongoUtils.getParamsFromJSON(jsonRequest);
            response = insertResource(table, customId, params);
        } catch (JongoBadRequestException ex){
            l.info("Failed to parse JSON arguments " + ex.getMessage());
            response = new JongoError(table, Response.Status.BAD_REQUEST, ex.getMessage());
        }
        
        return response;
    }
    
    public JongoResponse insertResource(final String table, final String customId, final Map<String, String> formParams){
        l.debug("Insert new " + database + "." + table + " with values: " + formParams);
        
        JongoResponse response = null;
        QueryParams params;
        try{
            params = QueryParams.valueOf(database, table, null, customId, formParams);
        }catch (IllegalArgumentException e){
            l.debug("Failed to generate query params " + e.getMessage());
            return new JongoError(table, Response.Status.BAD_REQUEST, e.getMessage());
        }
        
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
        } catch (Throwable ex){
            response = handleException(ex, params.getTable());
        }
        
        if(result == 0 && response == null){
            response = new JongoError(null, Response.Status.NO_CONTENT);
        }

        if(response == null){
            List<Row> results = new ArrayList<Row>();
            results.add(new Row(0));
            response = new JongoSuccess(null, results, Response.Status.CREATED);
        }
        return response;
    }
    
    public JongoResponse updateResource(final String table, final String customId, final String id, final String jsonRequest){
        l.debug("Update record " + id + " in table " + database + "." + table + " with values: " + jsonRequest);
        JongoResponse response = null;
        
        List<Row> results = null;
        
        QueryParams params;
        try{
            params = QueryParams.valueOf(database, table, id, customId);
        }catch (IllegalArgumentException e){
            l.debug("Failed to generate query params " + e.getMessage());
            return new JongoError(table, Response.Status.BAD_REQUEST, e.getMessage());
        }
        
        try {
            params.setParams(JongoUtils.getParamsFromJSON(jsonRequest));
            results = JDBCExecutor.update(params);
        } catch (Throwable ex){
            response = handleException(ex, table);
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
        
        QueryParams params;
        try{
            params = QueryParams.valueOf(database, table, id, customId);
        }catch (IllegalArgumentException e){
            l.debug("Failed to generate query params " + e.getMessage());
            return new JongoError(table, Response.Status.BAD_REQUEST, e.getMessage());
        }
        
        JongoResponse response = null;
        int result = 0;
        try {
            result = JDBCExecutor.delete(params);
        } catch (Throwable ex){
            response = handleException(ex, table);
        }
        
        if(result == 0 && response == null){
            response = new JongoError(table, Response.Status.NO_CONTENT);
        }

        if(response == null){
            List<Row> results = new ArrayList<Row>();
            results.add(new Row(0));
            response = new JongoSuccess(table, results, Response.Status.OK);
        }
        return response;
    }
    
    public JongoResponse findByColumn(final String table, final String col, final String arg, final LimitParam limit, final OrderParam order){
        l.debug("Geting resource from " + database + "." + table + " with " + col + " value " + arg);
        
        if(StringUtils.isEmpty(arg)){
            return new JongoError(table, Response.Status.BAD_REQUEST, "Invalid argument " + arg);
        }
        
        if(StringUtils.isEmpty(col)){
            return new JongoError(table, Response.Status.BAD_REQUEST, "Invalid argument " + col);
        }
        
        List<Row> results = null;
        
        QueryParams params;
        try{
            params = QueryParams.valueOf(database, table, null, col, limit, order);
        }catch (IllegalArgumentException e){
            l.debug("Failed to generate query params " + e.getMessage());
            return new JongoError(table, Response.Status.BAD_REQUEST, e.getMessage());
        }
        
        JongoResponse response = null;
        try {
            results = JDBCExecutor.findByColumn(params, JongoUtils.parseValue(arg));
        } catch (Throwable ex){
            response = handleException(ex, table);
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
        
        if(values == null)
            throw new IllegalArgumentException("Invalid null argument");
        
        JongoResponse response = null;
        List<Row> results = null;
        if(query == null){
            response = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
        }else{
            if(values.isEmpty()){
                try{
                    DynamicFinder df = DynamicFinder.valueOf(table, query);
                    results = JDBCExecutor.find(database, df, limit, order);
                } catch (Throwable ex){
                    response = handleException(ex, table);
                }
            }else{
                try{
                    DynamicFinder df = DynamicFinder.valueOf(table, query, values.toArray(new String []{}));
                    results = JDBCExecutor.find(database, df, limit, order, JongoUtils.parseValues(values));
                } catch (Throwable ex){
                    response = handleException(ex, table);
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
    
    public JongoResponse executeStoredProcedure(final String query, final String json){
        return new JongoError(query, Response.Status.SERVICE_UNAVAILABLE, "Not implemented");
    }
    
    private JongoResponse handleException(final Throwable t, final String resource){
        JongoResponse response;
        StringBuilder b;
        if(t instanceof SQLException){
            SQLException ex = (SQLException)t;
            b = new StringBuilder("Received a SQLException ");
            b.append(ex.getMessage());
            b.append(" state [");
            b.append(ex.getSQLState());
            b.append("] & code [");
            b.append(ex.getErrorCode());
            b.append("]");
            l.info(b.toString());
            response = new JongoError(resource, ex);
        }else if(t instanceof JongoBadRequestException){
            b = new StringBuilder("Received a JongoBadRequestException ");
            b.append(t.getMessage());
            l.info(b.toString());
            response = new JongoError(resource, Response.Status.BAD_REQUEST, t.getMessage());
        }else if(t instanceof IllegalArgumentException){
            b = new StringBuilder("Received an IllegalArgumentException ");
            b.append(t.getMessage());
            l.info(b.toString());
            response = new JongoError(resource, Response.Status.BAD_REQUEST, t.getMessage());
        }else{
            b = new StringBuilder("Received an Unhandled Exception ");
            b.append(t.getMessage());
            l.info(b.toString());
            response = new JongoError(resource, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
