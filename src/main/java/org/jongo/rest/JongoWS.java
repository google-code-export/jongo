package org.jongo.rest;

import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Public RESTful webservice which allows CRUD operations on a given resource.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface JongoWS {
    
    public Response get(final String table, final String format, final String id);
    public Response find(final String table, final String format, final String col, final String val);
    public Response findBy(final String table, final String format, final String query, final String value, final List<String> values);
    public Response insert(final String table, final String format, final MultivaluedMap<String, String> formParams);
    public Response update(final String table, final String format, final String id, final UriInfo ui);
    public Response delete(final String table, final String format, final String id);
    public Response query(final String query, final String format, final List<String> arguments);
    
}
