package org.jongo.rest;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.apache.commons.lang.StringUtils;
import org.jongo.enums.ErrorCode;
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
    @Path("get/{table}/{id}")
    @Produces({"application/xml","application/json"})
    @Override
    public String get(
            @PathParam("table") String table, 
            @PathParam("id") String id, 
            @DefaultValue("json") @QueryParam("format") String format
            ) {
        
        String q = "SELECT * FROM " + table + " WHERE id = " + id;
        List<RowResponse> results = JDBCExecutor.query(q);
        
        if(results == null || results.isEmpty()){
            JongoError error = new JongoError("No results for " + q, ErrorCode.E200);
            return (format.equalsIgnoreCase("json")) ? error.toJSON() : error.toXML();
        }
        
        JongoResponse r = new JongoResponse("kkk", results);
        return (format.equalsIgnoreCase("json")) ? r.toJSON() : r.toXML();
        
    }

    @GET
    @Path("find/{table}")
    @Produces({"application/xml","application/json"})
    @Override
    public String find(@PathParam("table") String table, 
                        @DefaultValue("json") @QueryParam("format") String format,
                        @QueryParam("col") final String col,
                        @QueryParam("val") final String val,
                        @DefaultValue("EQUALS") @QueryParam("op") final String op) {

        Operator operator = Operator.EQUALS;
        try{
            operator = Operator.valueOf(op.toUpperCase());
        }catch(IllegalArgumentException e){
            JongoError error = new JongoError(null, ErrorCode.E203);
            return (format.equalsIgnoreCase("json")) ? error.toJSON() : error.toXML();
        }
        
        final String q = "SELECT * FROM " + table + " WHERE " + col + " " + operator.sql()  + " " + val;
        List<RowResponse> results = JDBCExecutor.query(q);
        
        if(results == null || results.isEmpty()){
            JongoError error = new JongoError(null, ErrorCode.E200, "No results for " + q);
            return (format.equalsIgnoreCase("json")) ? error.toJSON() : error.toXML();
        }
        
        JongoResponse r = new JongoResponse("kkk", results);
        return (format.equalsIgnoreCase("json")) ? r.toJSON() : r.toXML();
    }

    @Override
    public String find(String table, String format, String col1, String val1, String col2, String val2, Operator op) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @PUT
    @Path("{table}")
    @Produces({"application/xml","application/json"})
    @Override
    public String insert(@PathParam("table") final String table, @DefaultValue("json") @QueryParam("format") String format, @QueryParam("cols") final List<String> cols, @QueryParam("vals") final List<String> vals) {
        final StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(table);
        query.append("(");
        query.append(StringUtils.join(cols, ","));
        query.append(") VALUES (");
        query.append(StringUtils.join(vals, ","));
        query.append(")");
        
        String result = JDBCExecutor.update(query.toString());
        
        if(result != null){
            JongoError error = new JongoError(null, ErrorCode.E204, result);
            return (format.equalsIgnoreCase("json")) ? error.toJSON() : error.toXML();
        }

        List<RowResponse> results = new ArrayList<RowResponse>();
        results.add(new RowResponse(0,null));
        JongoResponse r = new JongoResponse("kkk", results);
        return (format.equalsIgnoreCase("json")) ? r.toJSON() : r.toXML();
    }

    
    
}
