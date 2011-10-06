package org.jongo.rest;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.jongo.enums.ErrorCode;
import org.jongo.jdbc.JDBCConnectionFactory;
import org.jongo.jdbc.JongoJDBCConnection;
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
    @Produces({"application/xml","application/json"})
    @Override
    public String get(
            @PathParam("table") String table, 
            @PathParam("id") String id, 
            @DefaultValue("json") @QueryParam("format") String format
            ) {
        
        JongoJDBCConnection connection = JDBCConnectionFactory.getConnection();
        connection.getConnection();
        String q = "SELECT * FROM " + table + " WHERE id = " + id;
        List<RowResponse> results = connection.query(q);
        connection.close();
        
        if(results == null || results.isEmpty()){
            JongoError error = new JongoError("No results for " + q, ErrorCode.E200);
            return (format.equalsIgnoreCase("json")) ? error.toJSON() : error.toXML();
        }
        
        JongoResponse r = new JongoResponse("kkk", results);
        return (format.equalsIgnoreCase("json")) ? r.toJSON() : r.toXML();
        
    }
    
}
