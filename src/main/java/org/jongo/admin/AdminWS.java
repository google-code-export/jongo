/**
 * Copyright (C) 2011, 2012 Alejandro Ayuso
 *
 * This file is part of Jongo.
 * Jongo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * Jongo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jongo.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    public Response getJongoTable(final HttpServletRequest request, final String format);
    public Response getJongoTable(final String resourceId, final HttpServletRequest request, final String format);
    public Response insertJongoTable(final HttpServletRequest request, final String format, final MultivaluedMap<String, String> formParams);
    public Response updateJongoTable(final HttpServletRequest request, final String format, final String id, final UriInfo ui);
    public Response deleteJongoTable(final HttpServletRequest request, final String format, final String id);
    public Response getJongoQuery(final HttpServletRequest request, final String format);
    public Response getJongoQuery(final String queryId, final HttpServletRequest request, final String format);
    public Response insertJongoQuery(final HttpServletRequest request, final String format, final MultivaluedMap<String, String> formParams);
    public Response updateJongoQuery(final HttpServletRequest request, final String format, final String id, final UriInfo ui);
    public Response deleteJongoQuery(final HttpServletRequest request, final String format, final String id);
    public Response getJongoStatistics(final String format);
}
