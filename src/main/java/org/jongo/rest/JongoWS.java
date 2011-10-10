package org.jongo.rest;

import java.util.List;
import javax.ws.rs.core.Response;

/**
 * Public RESTful webservice which allows CRUD operations on a given resource.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface JongoWS {
    
    public Response get(final String table, final String format, final String id);
    public Response find(final String table, final String format, final String col, final String val);
    
    public Response findBy(final String table, final String format, final String query, final String value);
    public Response findBy(final String table, final String format, final String query, final List<String> values);
    
    /**
     * Inserts a new entity in the given table.
     * @param table the name of the table where to insert the new registry.
     * @param format the media type used to format the response (JSON by default)
     * @param cols a list of column names separated by commas (i.e.: cols=name,age,sex)
     * @param vals a list of values corresponding to the given columns (i.e.: vals='foo',23,'M')
     * @return a response for the operation. The response contains a status code which can be either 201
     * for success, 400 if the given arguments are incorrect or any other corresponding. 
     */
    public Response insert(final String table, final String format, final List<String> cols, final List<String> vals);
    public Response update(final String table, final String format, final String id, final List<String> cols, final List<String> vals);
    public Response delete(final String table, final String format, final String id);
    
}
