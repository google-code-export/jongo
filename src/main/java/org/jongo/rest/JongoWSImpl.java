package org.jongo.rest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import org.jongo.JongoUtils;
import org.jongo.jdbc.DynamicFinder;
import org.jongo.jdbc.JDBCExecutor;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoResponse;
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
    
    @GET
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response get(
            @PathParam("table") String table,
            @DefaultValue("json") @QueryParam("format") String format,
            @PathParam("id") String id ) {
        
        List<RowResponse> results;
        try {
            results = JDBCExecutor.get(table, id);
        } catch (SQLException ex) {
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.BAD_REQUEST);
            return error.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(results == null || results.isEmpty()){
            JongoError error = new JongoError(null, Response.Status.NOT_FOUND);
            return error.getResponse(format);
        }
        
        JongoResponse r = new JongoResponse(null, results);
        return r.getResponse(format);
        
    }

    @POST
    @Path("{table}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Override
    public Response insert(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, final MultivaluedMap<String, String> formParams) {
        int result;
        try {
            result = JDBCExecutor.insert(table, formParams);
        } catch (SQLException ex) {
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.BAD_REQUEST);
            return error.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(result == 0){
            JongoError error = new JongoError(null, Response.Status.NO_CONTENT);
            return error.getResponse(format);
        }

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0,null));
        JongoResponse r = new JongoResponse(null, results, Response.Status.CREATED);
        return r.getResponse(format);
    }

    @PUT
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response update(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id, @Context final UriInfo ui) {
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

        int result;
        try {
            result = JDBCExecutor.update(table, id, queryParams);
        } catch (SQLException ex) {
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.BAD_REQUEST);
            return error.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(result == 0){
            JongoError error = new JongoError(null, Response.Status.NO_CONTENT);
            return error.getResponse(format);
        }

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0,null));
        JongoResponse r = new JongoResponse(null, results, Response.Status.OK);
        return r.getResponse(format);
    }

    @DELETE
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response delete(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id) {
        int result = 0;
        try {
            result = JDBCExecutor.delete(table, id);
        } catch (SQLException ex) {
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.BAD_REQUEST);
            return error.getResponse(format);
        } catch (IllegalAccessException ex){
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.FORBIDDEN);
            return error.getResponse(format);
        } catch (Exception ex){
            l.error(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(result == 0){
            JongoError error = new JongoError(null, Response.Status.NO_CONTENT);
            return error.getResponse(format);
        }

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0,null));
        JongoResponse r = new JongoResponse(null, results, Response.Status.OK);
        return r.getResponse(format);
    }
    
    @GET
    @Path("{table}/{column}/{value}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response find(@PathParam("table") String table, 
                        @DefaultValue("json") @QueryParam("format") String format,
                        @PathParam("column") final String col,
                        @PathParam("value") final String val) {

        String q = "SELECT * FROM " + table + " WHERE " + col + " = ?";
        List<RowResponse> results;
        try {
            results = JDBCExecutor.find(q, JongoUtils.parseValue(val));
        } catch (SQLException ex) {
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.BAD_REQUEST);
            return error.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(results == null || results.isEmpty()){
            JongoError error = new JongoError(null, Response.Status.NOT_FOUND, "No results for " + q);
            return error.getResponse(format);
        }
        
        JongoResponse r = new JongoResponse(null, results);
        return r.getResponse(format);
    }
    
    @GET
    @Path("{table}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response findBy(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @QueryParam("query") String query, @QueryParam("value") String value, @QueryParam("values")  List<String> values) {
        List<RowResponse> results = null;
        if(query == null){
            results = JDBCExecutor.getTableMetaData(table);
            if(results == null || results.isEmpty()){
                JongoError error = new JongoError(null, Response.Status.NOT_FOUND, "Invalid table " + table);
                return error.getResponse(format);
            }
        }else{
            if(values.isEmpty()){
                if(value == null){
                    DynamicFinder df = DynamicFinder.valueOf(table, query);
                    results = JDBCExecutor.find(df);
                }else{
                    DynamicFinder df = DynamicFinder.valueOf(table, query, value);
                    results = JDBCExecutor.find(df, JongoUtils.parseValue(value));
                }

            }else{
                DynamicFinder df = DynamicFinder.valueOf(table, query, values.toArray(new String []{}));
                results = JDBCExecutor.find(df, JongoUtils.parseValues(values));
            }
            
            if(results == null || results.isEmpty()){
                JongoError error = new JongoError(null, Response.Status.NOT_FOUND, "No results for " + query);
                return error.getResponse(format);
            }
        }
        
        JongoResponse r = new JongoResponse(null, results);
        return r.getResponse(format);
    }

    @GET
    @Path("query/{query}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response query(@PathParam("query") String query, @QueryParam("format") String format, @QueryParam("args") List<String> arguments) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
