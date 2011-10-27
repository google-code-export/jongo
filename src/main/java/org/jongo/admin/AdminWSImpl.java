package org.jongo.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import org.jongo.JongoConfiguration;
import org.jongo.JongoUtils;
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
public class AdminWSImpl implements AdminWS {
    
    private static final Logger l = LoggerFactory.getLogger(AdminWSImpl.class);
    
    private static final String E403 = "<html><head></head><body>403</body></html>";

    @GET
    @Produces({ MediaType.TEXT_HTML })
    @Override
    public Response get(@Context HttpServletRequest request) {
        return get("index.html", request);
    }
    
    
    @GET
    @Path("{resource}")
    @Produces({ MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
    @Override
    public Response get(@PathParam("resource") final String resource, @Context HttpServletRequest request) {
        l.debug("Admin console connection from " + request.getRemoteAddr());
        l.debug(request.getPathInfo());
        
        JongoConfiguration conf = JongoConfiguration.instanceOf();
        
        if(!isAdminRequest(request)){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only localhost and " + conf.getAdminIp() + " are allowed");
            return Response.status(Response.Status.FORBIDDEN).entity(E403).type(MediaType.TEXT_HTML).build();
        }else{
            if(request.getPathInfo().equalsIgnoreCase("/jongo.js")){
                return readFileAndWriteToResponse(request.getPathInfo(), "text/javascript");
            }else if(request.getPathInfo().equalsIgnoreCase("/jongo.css")){
                return readFileAndWriteToResponse(request.getPathInfo(), "text/css");
            }else{
                return readFileAndWriteToResponse("/index.html", MediaType.TEXT_HTML.toString());
            }
        }
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
    
    @Override
    public Response deleteJongoTable(@Context HttpServletRequest request, String format, String id) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public Response deleteJongoQuery(@Context HttpServletRequest request, String format, String id) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        String query = null;
        List<RowResponse> results = null;
        try {
            if("all".equalsIgnoreCase(resource)){
                query = "SELECT * FROM " + table;
                results = JDBCExecutor.adminFind(table, query);
            }else{
                query = "SELECT * FROM " + table + " WHERE id = ?";
                results = JDBCExecutor.adminFind(table, query, JongoUtils.parseValue(resource));
            }
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
        } catch (Exception ex){
            l.info(ex.getMessage());
            JongoError error = new JongoError(null, Response.Status.INTERNAL_SERVER_ERROR);
            return error.getResponse(format);
        }
        
        if(results == null || results.isEmpty()){
            JongoError error = new JongoError(null, Response.Status.NOT_FOUND, "No results for " + query);
            return error.getResponse(format);
        }
        
        JongoResponse r = new JongoResponse(null, results);
        return r.getResponse(format);
    }
    
    private Response insertJongoResource(String table, MultivaluedMap<String, String> formParams, String format){
        int result = 0;
        try {
            result = JDBCExecutor.adminInsert(table, formParams);
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
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
        results.add(new RowResponse(0));
        JongoResponse r = new JongoResponse(null, results, Response.Status.CREATED);
        return r.getResponse(format);
    }
    
    private Response updateJongoResource(String table, String id, UriInfo ui, String format){
        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

        int result;
        try {
            result = JDBCExecutor.adminUpdate(table, id, queryParams);
        } catch (JongoJDBCException ex) {
            l.info(ex.getMessage());
            return ex.getResponse(format);
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
        results.add(new RowResponse(0));
        JongoResponse r = new JongoResponse(null, results, Response.Status.OK);
        return r.getResponse(format);
    }
    
    private boolean isAdminRequest(final HttpServletRequest request){
        JongoConfiguration conf = JongoConfiguration.instanceOf();
        return (request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1") || request.getRemoteAddr().equalsIgnoreCase(conf.getAdminIp()));
    }
    
    private Response readFileAndWriteToResponse(final String filePath, final String media){
        InputStream is = AdminWSImpl.class.getClass().getResourceAsStream("/org/jongo/admin" + filePath);
        StringBuilder b = new StringBuilder();
        BufferedReader r = null;
        
        String str = null;
        try{
            r = new BufferedReader(new InputStreamReader(is));
            while((str = r.readLine()) != null){
                b.append(str);
                b.append("\n");
            }
            return Response.status(Response.Status.OK).entity(b.toString()).type(media).build();
        }catch(IOException e){
            b = new StringBuilder("<html><head></head><body><h1>500</h1><p>");
            b.append(e.getMessage());
            b.append("</p></body></html>");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(b.toString()).type(media).build();
        }finally{
            if(r != null){ try { r.close(); } catch(Exception e){}}
            if(is != null){ try { is.close(); } catch(Exception e){}}
        }
    }

    
}