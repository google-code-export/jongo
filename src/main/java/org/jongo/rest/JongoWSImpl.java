package org.jongo.rest;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.jongo.enums.Operator;
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
        
        String q = "SELECT * FROM " + table + " WHERE id = " + id;
        List<RowResponse> results = JDBCExecutor.query(q);
        
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
        query.append(StringUtils.join(vals, ","));
        query.append(")");
        
        String result = JDBCExecutor.update(query.toString());
        
        if(result != null){
            JongoError error = new JongoError(null, Response.Status.BAD_REQUEST, result);
            return error.getResponse(format);
        }

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0,null));
        JongoResponse r = new JongoResponse(null, results, Response.Status.CREATED);
        return r.getResponse(format);
    }

    @Override
    public Response update(String table, String format, String id, List<String> cols, List<String> vals) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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

        String q = "SELECT * FROM " + table + " WHERE " + col + " = " + val;
        if(!StringUtils.isNumeric(val)){
            q = "SELECT * FROM " + table + " WHERE " + col + " = '" + val + "'";
        }
        
        
        List<RowResponse> results = JDBCExecutor.query(q);
        
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
    public Response findBy(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @QueryParam("query") String query, @QueryParam("value") String value) {
        String columnAndOperator = null;
        if(StringUtils.startsWith(query, "findBy")){
            columnAndOperator = splitCamelCase(StringUtils.substring(query, 6));
        }else if(StringUtils.startsWith(query, "findAllBy")){
            columnAndOperator = splitCamelCase(StringUtils.substring(query, 9));
        }else{
            JongoError error = new JongoError(null, Response.Status.BAD_REQUEST, "Unknown query. Use findBy or findAllBy.");
            return error.getResponse(format);
        }
        
        final String [] splitted = columnAndOperator.split(" ");
        final String col = splitted[0];
        final StringBuilder b = new StringBuilder();
        for(int i = 1; i < splitted.length; i++){
            b.append(splitted[i]);
        }
        Operator op = null;
        try{
            op = Operator.valueOf(b.toString().toUpperCase());
        }catch(IllegalArgumentException e){
            JongoError error = new JongoError(null, Response.Status.BAD_REQUEST, e.getMessage());
            return error.getResponse(format);
        }
        
        if(value == null){
            l.debug("SELECT * FROM " + table + " WHERE " + col + " " + op.sql());
        }else{
            l.debug("SELECT * FROM " + table + " WHERE " + col + " " + op.sql() + " " + value);
        }
        
        return null;
    }

    @Override
    public Response findBy(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @QueryParam("query") String query, @QueryParam("values")  List<String> values) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static String splitCamelCase(String s) {
        return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
    }
    
}
