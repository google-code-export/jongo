package org.jongo.rest;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.jongo.JongoUtils;
import org.jongo.jdbc.DynamicFinder;
import org.jongo.jdbc.JDBCExecutor;
import org.jongo.jdbc.exceptions.JongoJDBCException;
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
    @Path("{table}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response get(@PathParam("table") String table, @DefaultValue("json") @QueryParam("format") String format) {
        return get(table, format, null);
    }
    
    @GET
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response get(
            @PathParam("table") String table,
            @DefaultValue("json") @QueryParam("format") String format,
            @PathParam("id") String id ) {
        
        List<RowResponse> results;
        
        if(!StringUtils.isBlank(id) && id.equalsIgnoreCase("meta")){
            try{
                results = JDBCExecutor.getTableMetaData(table);
            } catch (JongoJDBCException ex) {
                l.info(ex.getMessage());
                return ex.getResponse(format);
            }
        }else{
            try {
                results = JDBCExecutor.get(table, id);
            } catch (JongoJDBCException ex) {
                l.info(ex.getMessage());
                l.info("" + ex.getSqlErrorCode());
                return ex.getResponse(format);
            } catch (Exception ex){
                l.info(ex.getMessage());
                JongoError error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
                return error.getResponse(format);
            }
        }
        
        if(results == null || results.isEmpty()){
            JongoError error = new JongoError(table, Response.Status.NOT_FOUND);
            return error.getResponse(format);
        }
        
        JongoResponse r = new JongoResponse(table, results);
        return r.getResponse(format);
        
    }

    @POST
    @Path("{table}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response insert(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, final String jsonRequest) {
        int result = 0;
        try {
            result = JDBCExecutor.insert(table, JongoUtils.getParamsFromJSON(jsonRequest));
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoError error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(result == 0){
            JongoError error = new JongoError(table, Response.Status.NO_CONTENT);
            return error.getResponse(format);
        }

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0));
        JongoResponse r = new JongoResponse(table, results, Response.Status.CREATED);
        return r.getResponse(format);
    }

    @PUT
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response update(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id, final String jsonRequest) {
        List<RowResponse> results = null;
        try {
            results = JDBCExecutor.update(table, id, JongoUtils.getParamsFromJSON(jsonRequest));
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoError error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(results == null){
            JongoError error = new JongoError(table, Response.Status.NO_CONTENT);
            return error.getResponse(format);
        }

        JongoResponse r = new JongoResponse(table, results, Response.Status.OK);
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
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
        } catch (Exception ex){
            l.error(ex.getMessage());
            JongoError error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(result == 0){
            JongoError error = new JongoError(table, Response.Status.NO_CONTENT);
            return error.getResponse(format);
        }

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0));
        JongoResponse r = new JongoResponse(table, results, Response.Status.OK);
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
            results = JDBCExecutor.find(table, q, JongoUtils.parseValue(val));
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoError error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(results == null || results.isEmpty()){
            JongoError error = new JongoError(table, Response.Status.NOT_FOUND, "No results for " + q);
            return error.getResponse(format);
        }
        
        JongoResponse r = new JongoResponse(table, results);
        return r.getResponse(format);
    }
    
    @GET
    @Path("{table}/dynamic/{query}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response findBy(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("query") String query, @QueryParam("value") String value, @QueryParam("values")  List<String> values) {
        List<RowResponse> results = null;
        if(query == null){
            JongoError error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
            return error.getResponse(format);
        }else{
            if(values.isEmpty()){
                if(value == null){
                    try{
                        DynamicFinder df = DynamicFinder.valueOf(table, query);
                        results = JDBCExecutor.find(df);
                    } catch (JongoJDBCException ex) {
                        l.info(ex.getMessage());
                        return ex.getResponse(format);
                    } catch (IllegalArgumentException ex){
                        l.info(ex.getMessage());
                        JongoError error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                        return error.getResponse(format);
                    }
                }else{
                    try{
                        DynamicFinder df = DynamicFinder.valueOf(table, query, value);
                        results = JDBCExecutor.find(df, JongoUtils.parseValue(value));
                    } catch (JongoJDBCException ex) {
                        l.info(ex.getMessage());
                        return ex.getResponse(format);
                    } catch (IllegalArgumentException ex){
                        l.info(ex.getMessage());
                        JongoError error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                        return error.getResponse(format);
                    }
                }

            }else{
                
                try{
                    DynamicFinder df = DynamicFinder.valueOf(table, query, values.toArray(new String []{}));
                    results = JDBCExecutor.find(df, JongoUtils.parseValues(values));
                } catch (JongoJDBCException ex) {
                    l.info(ex.getMessage());
                    return ex.getResponse(format);
                } catch (IllegalArgumentException ex){
                    l.info(ex.getMessage());
                    JongoError error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                    return error.getResponse(format);
                }
            }
            
            if(results == null || results.isEmpty()){
                JongoError error = new JongoError(table, Response.Status.NOT_FOUND, "No results for " + query);
                return error.getResponse(format);
            }
        }
        
        JongoResponse r = new JongoResponse(table, results);
        return r.getResponse(format);
    }

    @GET
    @Path("query/{query}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response query(@PathParam("query") String query, @DefaultValue("json") @QueryParam("format") String format, @QueryParam("args") List<String> arguments) {
        List<RowResponse> results;
        try {
            results = JDBCExecutor.executeQuery(query, JongoUtils.parseValues(arguments));
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoError error = new JongoError(query, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(results == null || results.isEmpty()){
            JongoError error = new JongoError(query, Response.Status.NOT_FOUND, "No results for " + query);
            return error.getResponse(format);
        }
        
        JongoResponse r = new JongoResponse(null, results);
        return r.getResponse(format);
    }
    
    
}
