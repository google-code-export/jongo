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
    public Response get(@PathParam("table") String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") String id ) {
        l.debug("Geting resource from " + table + " with id " + id);
        Response response = null;
        List<RowResponse> results = null;
        
        try{
            if(!StringUtils.isBlank(id) && id.equalsIgnoreCase("meta")){
                results = JDBCExecutor.getTableMetaData(table);
            }else{
                results = JDBCExecutor.get(table, id);
            }
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response =  ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoError error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            response =  error.getResponse(format);
        }
        
        if(results == null && response == null){
            JongoError error = new JongoError(table, Response.Status.NOT_FOUND);
            response =  error.getResponse(format);
        }
        
        if(response == null){
            JongoResponse r = new JongoResponse(table, results);
            response = r.getResponse(format);
        }
        
        l.debug(response.getEntity().toString());
        return response;
        
    }

    @POST
    @Path("{table}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response insert(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, final String jsonRequest) {
        l.debug("Insert new " + table + " with values: " + jsonRequest);
        Response response = null;
        int result = 0;
        try {
            result = JDBCExecutor.insert(table, JongoUtils.getParamsFromJSON(jsonRequest));
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response = ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoError error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            response =  error.getResponse(format);
        }
        
        if(result == 0 && response == null){
            JongoError error = new JongoError(table, Response.Status.NO_CONTENT);
            response =  error.getResponse(format);
        }

        if(response == null){
            List<RowResponse> results = new ArrayList<RowResponse>();
            results.add(new RowResponse(0));
            JongoResponse r = new JongoResponse(table, results, Response.Status.CREATED);
            response =  r.getResponse(format);
        }
        
        l.debug(response.getEntity().toString());
        return response;
    }

    @PUT
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public Response update(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id, final String jsonRequest) {
        l.debug("Update record " + id + " in table " + table + " with values: " + jsonRequest);
        Response response = null;
        List<RowResponse> results = null;
        try {
            results = JDBCExecutor.update(table, id, JongoUtils.getParamsFromJSON(jsonRequest));
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response = ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoError error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            response = error.getResponse(format);
        }
        
        if(results == null && response == null){
            JongoError error = new JongoError(table, Response.Status.NO_CONTENT);
            response =  error.getResponse(format);
        }

        if(response == null){
            JongoResponse r = new JongoResponse(table, results, Response.Status.OK);
            response = r.getResponse(format);
        }
        
        l.debug(response.getEntity().toString());
        return response;
    }

    @DELETE
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response delete(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("id") final String id) {
        l.debug("Delete record " + id + " from table " + table);
        Response response = null;
        int result = 0;
        try {
            result = JDBCExecutor.delete(table, id);
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response = ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoError error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            response = error.getResponse(format);
        }
        
        if(result == 0 && response == null){
            JongoError error = new JongoError(table, Response.Status.NO_CONTENT);
            response = error.getResponse(format);
        }

        if(response == null){
            List<RowResponse> results = new ArrayList<RowResponse>();
            results.add(new RowResponse(0));
            JongoResponse r = new JongoResponse(table, results, Response.Status.OK);
            response = r.getResponse(format);
        }
        
        l.debug(response.getEntity().toString());
        return response;
    }
    
    @GET
    @Path("{table}/{column}/{value}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response find(@PathParam("table") String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("column") final String col, @PathParam("value") final String val) {
        l.debug("Geting resource from " + table + " with " + col + " value " + val);
        Response response = null;
        List<RowResponse> results = null;
        try {
            results = JDBCExecutor.findByColumn(table, col, JongoUtils.parseValue(val));
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response =  ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoError error = new JongoError(table, Response.Status.INTERNAL_SERVER_ERROR);
            response =  error.getResponse(format);
        }
        
        if((results == null || results.isEmpty()) && response == null ){
            JongoError error = new JongoError(table, Response.Status.NOT_FOUND, "No results for " + table + " with " + col + " = " + val);
            response =  error.getResponse(format);
        }
        
        if(response == null){
            JongoResponse r = new JongoResponse(table, results);
            response =  r.getResponse(format);
        }
        
        l.debug(response.getEntity().toString());
        return response;
    }
    
    @GET
    @Path("{table}/dynamic/{query}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response findBy(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @PathParam("query") String query, @QueryParam("value") String value, @QueryParam("values")  List<String> values) {
        l.debug("Find resource from " + table + " with " + query);
        Response response = null;
        List<RowResponse> results = null;
        if(query == null){
            JongoError error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
            response =  error.getResponse(format);
        }else{
            if(values.isEmpty()){
                if(value == null){
                    try{
                        DynamicFinder df = DynamicFinder.valueOf(table, query);
                        results = JDBCExecutor.find(df);
                    } catch (JongoJDBCException ex) {
                        l.info("Received a JongoJDBCException " + ex.getMessage());
                        response =  ex.getResponse(format);
                    } catch (IllegalArgumentException ex){
                        l.info("Received an Unhandled Exception " + ex.getMessage());
                        JongoError error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                        response =  error.getResponse(format);
                    }
                }else{
                    try{
                        DynamicFinder df = DynamicFinder.valueOf(table, query, value);
                        results = JDBCExecutor.find(df, JongoUtils.parseValue(value));
                    } catch (JongoJDBCException ex) {
                        l.info("Received a JongoJDBCException " + ex.getMessage());
                        response =  ex.getResponse(format);
                    } catch (IllegalArgumentException ex){
                        l.info("Received an Unhandled Exception " + ex.getMessage());
                        JongoError error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                        response =  error.getResponse(format);
                    }
                }

            }else{
                
                try{
                    DynamicFinder df = DynamicFinder.valueOf(table, query, values.toArray(new String []{}));
                    results = JDBCExecutor.find(df, JongoUtils.parseValues(values));
                } catch (JongoJDBCException ex) {
                    l.info("Received a JongoJDBCException " + ex.getMessage());
                    response =  ex.getResponse(format);
                } catch (IllegalArgumentException ex){
                    l.info("Received an Unhandled Exception " + ex.getMessage());
                    JongoError error = new JongoError(table, Response.Status.BAD_REQUEST, "Invalid query " + query);
                    response =  error.getResponse(format);
                }
            }
            
            if((results == null || results.isEmpty()) && response == null){
                JongoError error = new JongoError(table, Response.Status.NOT_FOUND, "No results for " + query);
                response =  error.getResponse(format);
            }
        }
        if(response == null){
            JongoResponse r = new JongoResponse(table, results);
            response =  r.getResponse(format);
        }
        
        l.debug(response.getEntity().toString());
        return response;
    }

    @GET
    @Path("query/{query}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response query(@PathParam("query") String query, @DefaultValue("json") @QueryParam("format") String format, @QueryParam("args") List<String> arguments) {
        l.debug("Executing Complex Query " + query);
        Response response = null;
        List<RowResponse> results = null;
        try {
            results = JDBCExecutor.executeQuery(query, JongoUtils.parseValues(arguments));
        } catch (JongoJDBCException ex) {
            l.info("Received a JongoJDBCException " + ex.getMessage());
            response =  ex.getResponse(format);
        } catch (Exception ex){
            l.info("Received an Unhandled Exception " + ex.getMessage());
            JongoError error = new JongoError(query, Response.Status.INTERNAL_SERVER_ERROR);
            response =  error.getResponse(format);
        }
        
        if((results == null || results.isEmpty()) && response == null){
            JongoError error = new JongoError(query, Response.Status.NOT_FOUND, "No results for " + query);
            response =  error.getResponse(format);
        }
        
        if(response == null){
            JongoResponse r = new JongoResponse(null, results);
            response =  r.getResponse(format);
        }
        
        l.debug(response.getEntity().toString());
        return response;
        
    }
    
    
}
