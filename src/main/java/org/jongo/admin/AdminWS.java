package org.jongo.admin;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface AdminWS {
    public Response getJongoTable(final String resourceId, final HttpServletRequest request, final String format);
    public Response insertJongoTable(final HttpServletRequest request, final String format, final MultivaluedMap<String, String> formParams);
    public Response updateJongoTable(final HttpServletRequest request, final String format, final String id, final UriInfo ui);
    public Response deleteJongoTable(final HttpServletRequest request, final String format, final String id);
    public Response getJongoQuery(final String queryId, final HttpServletRequest request, final String format);
    public Response insertJongoQuery(final HttpServletRequest request, final String format, final MultivaluedMap<String, String> formParams);
    public Response updateJongoQuery(final HttpServletRequest request, final String format, final String id, final UriInfo ui);
    public Response deleteJongoQuery(final HttpServletRequest request, final String format, final String id);
}
