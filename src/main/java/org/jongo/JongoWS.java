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

package org.jongo;

import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Public RESTful webservice which allows CRUD operations on a given resource.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface JongoWS {
    
    public Response dbMeta(String database, String format);
    public Response resourceMeta(String database, String resource, String format);
    public Response get(String database, String resource, String format, String idField, String id, UriInfo ui);
    public Response getAll(String database, String resource, String format, UriInfo ui);
    public Response find(String database, String resource, String format, String col, String arg, UriInfo ui);
    public Response findBy(String database, String resource, String format, String query, List<String> args, UriInfo ui);
    public Response insert(String database, String resource, String format, String idField, String jsonRequest);
    public Response insert(String database, String resource, String format, String idField, MultivaluedMap<String, String> formParams);
    public Response update(String database, String resource, String format, String idField, String id, String jsonRequest);
    public Response delete(String database, String resource, String format, String idField, String id);
    public Response storedProcedure(String database, String query, String format, String jsonRequest);
    public Response getJongoStatistics(String format);
    
}
