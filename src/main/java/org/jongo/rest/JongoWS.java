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
    
    public Response get(final String format, final UriInfo ui);
    public Response get(final String table, final String format, final UriInfo ui);
    public Response get(final String table, final String format, final String id, final UriInfo ui);
    public Response find(final String table, final String format, final String col, final String arg, final UriInfo ui);
    public Response findBy(final String table, final String format, final String query, final List<String> args, final UriInfo ui);
    public Response insert(final String table, final String format, final String jsonRequest, final UriInfo ui);
    public Response insert(final String table, final String format, final MultivaluedMap<String, String> formParams, final UriInfo ui);
    public Response update(final String table, final String format, final String id, final String jsonRequest, final UriInfo ui);
    public Response delete(final String table, final String format, final String id, final UriInfo ui);
    public Response query(final String query, final String format, final List<String> arguments, final UriInfo ui);
    
}
