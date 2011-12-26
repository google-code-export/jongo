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

package org.jongo.admin;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
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
import org.jongo.JongoConfiguration;
import org.jongo.JongoUtils;
import org.jongo.Usage;
import org.jongo.jdbc.AdminJDBCExecutor;
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
public class AdminWSImpl implements AdminWS {
    
    private static final Logger l = LoggerFactory.getLogger(AdminWSImpl.class);
    private static final String E403 = "<html><head></head><body>403</body></html>";

    @Override
    @GET @Path("table") @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getJongoTable(@Context  HttpServletRequest request, @DefaultValue("json") @QueryParam("format") String format) {
            return getJongoTable(null, request, format);
    }
    
    @Override
    @GET @Path("table/{resource}") @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getJongoTable(@PathParam("resource") String resourceId, @Context  HttpServletRequest request, @DefaultValue("json") @QueryParam("format") String format) {
        if(!isAdminRequest(request)){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only admin IPs are allowed");
            return Response.status(Response.Status.FORBIDDEN).entity(E403).type(MediaType.TEXT_HTML).build();
        }else{
            return getJongoResource(resourceId, "JongoTable", format);
        }
    }
    
    @Override
    @POST @Path("table") @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response insertJongoTable(@Context HttpServletRequest request, @DefaultValue("json") @QueryParam("format") String format, MultivaluedMap<String, String> formParams) {
        if(!isAdminRequest(request)){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only admin IPs are allowed");
            return Response.status(Response.Status.FORBIDDEN).entity(E403).type(MediaType.TEXT_HTML).build();
        }else{
            return insertJongoResource("JongoTable", formParams, format);
        }
    }
    
    @Override
    @PUT @Path("table/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response updateJongoTable(@Context HttpServletRequest request, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id, @Context final UriInfo ui) {
        if(!isAdminRequest(request)){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only admin IPs are allowed");
            return Response.status(Response.Status.FORBIDDEN).entity(E403).type(MediaType.TEXT_HTML).build();
        }else{
            return updateJongoResource("JongoTable", id, ui, format);
        }
    }
    
    @DELETE
    @Path("table/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response deleteJongoTable(@Context HttpServletRequest request, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id) {
        l.debug("Deleting admin table with ID " + id);
        if(!isAdminRequest(request)){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only admin IPs are allowed");
            return Response.status(Response.Status.FORBIDDEN).entity(E403).type(MediaType.TEXT_HTML).build();
        }else{
            return deleteJongoResource("JongoTable", id, format);
        }
    }
    
    @Override
    @GET @Path("query")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getJongoQuery(@Context  HttpServletRequest request, @DefaultValue("json") @QueryParam("format") String format) {
        return getJongoQuery(null, request, format);
    }
    
    @Override
    @GET @Path("query/{qid}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getJongoQuery(@PathParam("qid") String queryId, @Context  HttpServletRequest request, @DefaultValue("json") @QueryParam("format") String format) {
        if(!isAdminRequest(request)){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only admin IPs are allowed");
            return Response.status(Response.Status.FORBIDDEN).entity(E403).type(MediaType.TEXT_HTML).build();
        }else{
            return getJongoResource(queryId, "JongoQuery", format);
        }
    }

    @DELETE
    @Path("query/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response deleteJongoQuery(@Context HttpServletRequest request, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id) {
        if(!isAdminRequest(request)){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only admin IPs are allowed");
            return Response.status(Response.Status.FORBIDDEN).entity(E403).type(MediaType.TEXT_HTML).build();
        }else{
            return deleteJongoResource("JongoQuery", id, format);
        }
    }
    
    @Override
    @POST @Path("query") @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response insertJongoQuery(@Context HttpServletRequest request, @DefaultValue("json") @QueryParam("format") String format, MultivaluedMap<String, String> formParams) {
        if(!isAdminRequest(request)){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only admin IPs are allowed");
            return Response.status(Response.Status.FORBIDDEN).entity(E403).type(MediaType.TEXT_HTML).build();
        }else{
            return insertJongoResource("JongoQuery", formParams, format);
        }
    }

    @Override
    @PUT @Path("query/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response updateJongoQuery(@Context HttpServletRequest request, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id, @Context final UriInfo ui) {
        if(!isAdminRequest(request)){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only admin IPs are allowed");
            return Response.status(Response.Status.FORBIDDEN).entity(E403).type(MediaType.TEXT_HTML).build();
        }else{
            return updateJongoResource("JongoQuery", id, ui, format);
        }
    }
    
    private Response getJongoResource(String resource, String table, String format){
        List<RowResponse> results = null;
        try {
            if(StringUtils.isBlank(resource)){
                results = AdminJDBCExecutor.findAll(table);
            }else if(StringUtils.isNumeric(resource)) {
                results = AdminJDBCExecutor.find(table,  JongoUtils.parseValue(resource));
            }
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoResponse error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(results == null || results.isEmpty()){
            JongoResponse error = new JongoError(null, Response.Status.NOT_FOUND, "No results");
            return error.getResponse(format);
        }
        
        JongoResponse r = new JongoSuccess(null, results);
        return r.getResponse(format);
    }
    
    private Response insertJongoResource(String table, MultivaluedMap<String, String> formParams, String format){
        int result = 0;
        try {
            result = AdminJDBCExecutor.insert(table, formParams);
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoResponse error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(result == 0){
            JongoResponse error = new JongoError(null, Response.Status.NO_CONTENT);
            return error.getResponse(format);
        }

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0));
        JongoResponse r = new JongoSuccess(null, results, Response.Status.CREATED);
        return r.getResponse(format);
    }
    
    private Response updateJongoResource(String table, String id, UriInfo ui, String format){
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

        int result;
        try {
            result = AdminJDBCExecutor.update(table, id, queryParams);
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoResponse error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(result == 0){
            JongoResponse error = new JongoError(null, Response.Status.NO_CONTENT);
            return error.getResponse(format);
        }

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0));
        JongoResponse r = new JongoSuccess(null, results, Response.Status.OK);
        return r.getResponse(format);
    }
    
    private Response deleteJongoResource(String table, String id, String format){
        int result = 0;
        try {
            result = AdminJDBCExecutor.delete(table, id);
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
        } catch (Exception ex){
            l.error(ex.getMessage());
            JongoResponse error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(result == 0){
            JongoResponse error = new JongoError(null, Response.Status.NO_CONTENT);
            return error.getResponse(format);
        }

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0));
        JongoResponse r = new JongoSuccess(null, results, Response.Status.OK);
        return r.getResponse(format);
    }
    
    private boolean isAdminRequest(final HttpServletRequest request){
        JongoConfiguration conf = JongoConfiguration.instanceOf();
        return (request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1") || request.getRemoteAddr().equalsIgnoreCase(conf.getAdminIp()));
    }

    @Override
    @GET @Path("stats") @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getJongoStatistics(@DefaultValue("json") @QueryParam("format") String format) {
        Usage u = Usage.getInstance();
        return u.getResponse(format);
    }
    
    
}
