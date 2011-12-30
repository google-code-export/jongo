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

package org.jongo.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang.StringUtils;
import org.jongo.JongoUtils;
import org.jongo.Usage;
import org.jongo.exceptions.JongoBadRequestException;
import org.jongo.jdbc.DynamicFinder;
import org.jongo.jdbc.JDBCExecutor;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.jdbc.exceptions.JongoJDBCException;
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
@Path("/")
public class JongoWSImpl implements JongoWS {
    
    private static final Logger l = LoggerFactory.getLogger(JongoWSImpl.class);
    private static final Usage u = Usage.getInstance();
    
    @GET
    @Path("{table}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response get(@PathParam("table") String table, @DefaultValue("json") @QueryParam("format") String format, @Context final UriInfo ui) {
        return get(table, format, null, ui);
    }
    
    @GET
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response get(@PathParam("table") String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") String id, @Context final UriInfo ui) {
        final String database = ui.getBaseUri().getPath().replaceAll("/", "");
        l.debug("Geting resource from " + database + "." + table + " with id " + id);
        
        final Long start = System.nanoTime();
        
        Response response = null;
        List<RowResponse> results = null;
        
        MultivaluedMap<String, String> pathParams = ui.getQueryParameters();
        LimitParam limit = LimitParam.valueOf(pathParams);
        OrderParam order = OrderParam.valueOf(pathParams);
        
        try{
            if(!StringUtils.isBlank(id) && id.equalsIgnoreCase("meta")){
                results = JDBCExecutor.getTableMetaData(database, table);
            }else{
                results = JDBCExecutor.get(database, table, id, limit, order);
            }
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response =  ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoResponse error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            response =  error.getResponse(format);
        }
        
        if(results == null && response == null){
            JongoResponse error = new JongoError(table, Response.Status.NOT_FOUND);
            response =  error.getResponse(format);
        }
        
        if(response == null){
            JongoResponse r = new JongoSuccess(table, results);
            response = r.getResponse(format);
        }
        
        u.addRead(perf("Time taken to perform a read", start), response.getStatus());
        return response;
        
    }

    @POST
    @Path("{table}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response insert(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, final String jsonRequest, @Context final UriInfo ui) {
        final String database = ui.getBaseUri().getPath().replaceAll("/", "");
        l.debug("Insert new " + database + "." + table + " with JSON values: " + jsonRequest);
        
        final Long start = System.nanoTime();
        
        Response response = null;
        int result = 0;
        try {
            result = JDBCExecutor.insert(database, table, JongoUtils.getParamsFromJSON(jsonRequest));
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException ");
            response = ex.getResponse(format);
        } catch (JongoBadRequestException ex){
            l.info("Received a JongoBadRequestException " + ex.getMessage());
            ex.setResource(table);
            response = ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoResponse error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            response =  error.getResponse(format);
        }
        
        if(result == 0 && response == null){
            JongoResponse error = new JongoError(table, Response.Status.NO_CONTENT);
            response =  error.getResponse(format);
        }

        if(response == null){
            List<RowResponse> results = new ArrayList<RowResponse>();
            results.add(new RowResponse(0));
            JongoResponse r = new JongoSuccess(table, results, Response.Status.CREATED);
            response =  r.getResponse(format);
        }
        
        u.addCreate(perf("Time taken to perform a create", start), response.getStatus());
        return response;
    }
    
    @POST
    @Path("{table}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Override
    public Response insert(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, final MultivaluedMap<String, String> formParams, @Context final UriInfo ui) {
        final String database = ui.getBaseUri().getPath().replaceAll("/", "");
        l.debug("Insert new " + database + "." + table + " with values: " + formParams);
        
        final Long start = System.nanoTime();
        
        Response response = null;
        if(formParams.size() == 0)
            response = new JongoError(table, Response.Status.BAD_REQUEST, "No arguments given").getResponse(format);
        
        int result = 0;
        try {
            if(response == null)
                result = JDBCExecutor.insert(database, table, formParams);
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response = ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoResponse error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            response = error.getResponse(format);
        }
        
        if(result == 0 && response == null){
            JongoResponse error = new JongoError(null, Response.Status.NO_CONTENT);
            response = error.getResponse(format);
        }

        if(response == null){
            List<RowResponse> results = new ArrayList<RowResponse>();
            results.add(new RowResponse(0));
            JongoResponse r = new JongoSuccess(null, results, Response.Status.CREATED);
            response = r.getResponse(format);
        }
        
        u.addCreate(perf("Time taken to perform a create", start), response.getStatus());
        return response;
    }

    @PUT
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response update(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id, final String jsonRequest, @Context final UriInfo ui) {
        final String database = ui.getBaseUri().getPath().replaceAll("/", "");
        l.debug("Update record " + id + " in table " + database + "." + table + " with values: " + jsonRequest);
        
        final Long start = System.nanoTime();
        
        Response response = null;
        List<RowResponse> results = null;
        try {
            results = JDBCExecutor.update(database, table, id, JongoUtils.getParamsFromJSON(jsonRequest));
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response = ex.getResponse(format);
        } catch (JongoBadRequestException ex){
            l.info("Received a JongoBadRequestException " + ex.getMessage());
            ex.setResource(table);
            response = ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoResponse error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            response = error.getResponse(format);
        }
        
        if(results == null && response == null){
            JongoResponse error = new JongoError(table, Response.Status.NO_CONTENT);
            response =  error.getResponse(format);
        }

        if(response == null){
            JongoResponse r = new JongoSuccess(table, results, Response.Status.OK);
            response = r.getResponse(format);
        }
        
        u.addUpdate(perf("Time taken to perform a update", start), response.getStatus());
        return response;
    }
    
    @DELETE
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response delete(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id, @Context final UriInfo ui) {
        final String database = ui.getBaseUri().getPath().replaceAll("/", "");
        l.debug("Delete record " + id + " from table " + database + "." + table);
        
        final Long start = System.nanoTime();
        
        Response response = null;
        int result = 0;
        try {
            result = JDBCExecutor.delete(database, table, id);
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response = ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoResponse error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            response = error.getResponse(format);
        }
        
        if(result == 0 && response == null){
            JongoResponse error = new JongoError(table, Response.Status.NO_CONTENT);
            response = error.getResponse(format);
        }

        if(response == null){
            List<RowResponse> results = new ArrayList<RowResponse>();
            results.add(new RowResponse(0));
            JongoResponse r = new JongoSuccess(table, results, Response.Status.OK);
            response = r.getResponse(format);
        }
        
        u.addDelete(perf("Time taken to perform a delete", start), response.getStatus());
        return response;
    }
    
    @GET
    @Path("{table}/{column}/{value}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response find(@PathParam("table") String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("column") final String col, @PathParam("value") final String val, @Context final UriInfo ui) {
        final String database = ui.getBaseUri().getPath().replaceAll("/", "");
        l.debug("Geting resource from " + database + "." + table + " with " + col + " value " + val);
        
        final Long start = System.nanoTime();
        
        Response response = null;
        List<RowResponse> results = null;
        try {
            results = JDBCExecutor.findByColumn(database, table, col, JongoUtils.parseValue(val));
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response =  ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoResponse error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            response =  error.getResponse(format);
        }
        
        if((results == null || results.isEmpty()) && response == null ){
            JongoResponse error = new JongoError(table, Response.Status.NOT_FOUND, "No results for " + table + " with " + col + " = " + val);
            response =  error.getResponse(format);
        }
        
        if(response == null){
            JongoResponse r = new JongoSuccess(table, results);
            response =  r.getResponse(format);
        }
        
        u.addRead(perf("Time taken to perform a read", start), response.getStatus());
        return response;
    }
    
    @GET
    @Path("{table}/dynamic/{query}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response findBy(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("query") String query, @QueryParam("value") String value, @QueryParam("values")  List<String> values, @Context final UriInfo ui) {
        final String database = ui.getBaseUri().getPath().replaceAll("/", "");
        l.debug("Find resource from " + database + "." + table + " with " + query);
        
        final Long start = System.nanoTime();
        
        Response response = null;
        List<RowResponse> results = null;
        if(query == null){
            JongoResponse error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
            response =  error.getResponse(format);
        }else{
            if(values.isEmpty()){
                if(value == null){
                    try{
                        DynamicFinder df = DynamicFinder.valueOf(table, query);
                        results = JDBCExecutor.find(database, df);
                    } catch (JongoJDBCException ex) {
                        l.info("Received a JongoJDBCException " + ex.getMessage());
                        response =  ex.getResponse(format);
                    } catch (JongoBadRequestException ex){
                        l.info("Received a JongoBadRequestException " + ex.getMessage());
                        ex.setResource(table);
                        response = ex.getResponse(format);
                    } catch (IllegalArgumentException ex){
                        l.info("Received an Unhandled Exception " + ex.getMessage());
                        JongoResponse error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                        response =  error.getResponse(format);
                    }
                }else{
                    try{
                        DynamicFinder df = DynamicFinder.valueOf(table, query, value);
                        results = JDBCExecutor.find(database, df, JongoUtils.parseValue(value));
                    } catch (JongoJDBCException ex) {
                        l.info("Received a JongoJDBCException " + ex.getMessage());
                        response =  ex.getResponse(format);
                    } catch (JongoBadRequestException ex){
                        l.info("Received a JongoBadRequestException " + ex.getMessage());
                        ex.setResource(table);
                        response = ex.getResponse(format);
                    } catch (IllegalArgumentException ex){
                        l.info("Received an Unhandled Exception " + ex.getMessage());
                        JongoResponse error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                        response =  error.getResponse(format);
                    }
                }

            }else{
                
                try{
                    DynamicFinder df = DynamicFinder.valueOf(table, query, values.toArray(new String []{}));
                    results = JDBCExecutor.find(database, df, JongoUtils.parseValues(values));
                } catch (JongoJDBCException ex) {
                    l.info("Received a JongoJDBCException " + ex.getMessage());
                    response =  ex.getResponse(format);
                } catch (JongoBadRequestException ex){
                    l.info("Received a JongoBadRequestException " + ex.getMessage());
                    ex.setResource(table);
                    response = ex.getResponse(format);
                } catch (IllegalArgumentException ex){
                    l.info("Received an Unhandled Exception " + ex.getMessage());
                    JongoResponse error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                    response =  error.getResponse(format);
                }
            }
            
            if((results == null || results.isEmpty()) && response == null){
                JongoResponse error = new JongoError(table, Response.Status.NOT_FOUND, "No results for " + query);
                response =  error.getResponse(format);
            }
        }
        if(response == null){
            JongoResponse r = new JongoSuccess(table, results);
            response =  r.getResponse(format);
        }
        
        u.addDynamic(perf("Time taken to perform a dynamic finder", start), response.getStatus());
        return response;
    }

    @GET
    @Path("query/{query}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response query(@PathParam("query") String query, @DefaultValue("json") @QueryParam("format") String format, @QueryParam("args") List<String> arguments, @Context final UriInfo ui) {
        final String database = ui.getBaseUri().getPath().replaceAll("/", "");
        l.debug("Executing Complex Query " + query);
        
        final Long start = System.nanoTime();
        
        Response response = null;
        List<RowResponse> results = null;
        try {
            results = JDBCExecutor.executeQuery(database, query, JongoUtils.parseValues(arguments));
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response =  ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoResponse error = new JongoError(query, Response.Status.INTERNAL_SERVER_ERROR);
            response =  error.getResponse(format);
        }
        
        if((results == null || results.isEmpty()) && response == null){
            JongoResponse error = new JongoError(query, Response.Status.NOT_FOUND, "No results for " + query);
            response =  error.getResponse(format);
        }
        
        if(response == null){
            JongoResponse r = new JongoSuccess(query, results);
            response =  r.getResponse(format);
        }
        
        u.addQuery(perf("Time taken to execute a query", start), response.getStatus());
        return response;
        
    }
    
    private Long perf(final String message, final Long start){
        final Long dur = System.nanoTime() - start;
        StringBuilder b = new StringBuilder(message);
        b.append(" ");
        b.append(TimeUnit.NANOSECONDS.toMillis(dur));
        b.append("ms");
        l.debug(b.toString());
        return dur;
    }
}
