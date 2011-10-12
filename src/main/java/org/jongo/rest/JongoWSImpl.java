package org.jongo.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
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
@Path("jongo/")
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
        
        String q = "SELECT * FROM " + table + " WHERE id = ?";
        List<RowResponse> results = JDBCExecutor.find(q, id);
        
        if(results == null || results.isEmpty()){
            JongoError error = new JongoError(null, Response.Status.NOT_FOUND, "No results for " + q);
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
    public Response insert(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @FormParam("cols") final List<String> cols, @FormParam("vals") final List<String> vals) {
        final StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(table);
        query.append("(");
        query.append(StringUtils.join(cols, ","));
        query.append(") VALUES (");
        query.append(StringUtils.removeEnd(StringUtils.repeat("?,", cols.size()), ","));
        query.append(")");
        
        int result = JDBCExecutor.update(query.toString(), JongoUtils.parseValues(vals));
        
        if(result == 0){
            JongoError error = new JongoError(null, Response.Status.BAD_REQUEST);
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

        List<String> params = new ArrayList<String>(queryParams.size());
        final StringBuilder query = new StringBuilder("UPDATE ");
        query.append(table);
        query.append(" SET ");
        
        for(String k : queryParams.keySet()){
            query.append(k); query.append(" = ?,");
            params.add(queryParams.getFirst(k));
        }
        
        query.deleteCharAt(query.length() - 1);
        query.append(" WHERE id = ?");
        params.add(id);
        
        int result = JDBCExecutor.update(query.toString(), JongoUtils.parseValues(params));
        
        if(result == 0){
            JongoError error = new JongoError(null, Response.Status.BAD_REQUEST);
            return error.getResponse(format);
        }
        l.debug(query.toString() + " " + params);

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0,null));
        JongoResponse r = new JongoResponse(null, results, Response.Status.CREATED);
        return r.getResponse(format);
    }

    @DELETE
    @Path("{table}/{id}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public Response delete(String table, String format, String id) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        List<RowResponse> results = JDBCExecutor.find(q, JongoUtils.parseValue(val));
        
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
            String sql = null;
            if(values.isEmpty()){
                if(value == null){
                    DynamicFinder df = DynamicFinder.valueOf(table, query);
                    sql = "SELECT * FROM " + table + df.getSql() ;
                    results = JDBCExecutor.find(df);
                }else{
                    DynamicFinder df = DynamicFinder.valueOf(table, query, value);
                    sql = "SELECT * FROM " + table + df.getSql() ;
                    results = JDBCExecutor.find(df, JongoUtils.parseValue(value));
                }

            }else{
                DynamicFinder df = DynamicFinder.valueOf(table, query, values.toArray(new String []{}));
                sql = "SELECT * FROM " + table + df.getSql();
                results = JDBCExecutor.find(df, JongoUtils.parseValues(values));
            }
            
            if(results == null || results.isEmpty()){
                JongoError error = new JongoError(null, Response.Status.NOT_FOUND, "No results for " + sql);
                return error.getResponse(format);
            }
        }
        
        JongoResponse r = new JongoResponse(null, results);
        return r.getResponse(format);
    }
}
