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
    
    /**
     * REST gateway for database metadata
     * @param database name of the database we want to access
     * @return depends on database implementations but probably, a list of the
     * tables defined in the database or schema.
     */
    public Response dbMeta(String database);
    
    /**
     * REST gateway for resource metadata. A resource can be a table, a view, etc.
     * @param database name of the database the resource belongs to
     * @param resource name of the resource we want to access
     * @return metadata about the resource, like its fields.
     */
    public Response resourceMeta(String database, String resource);
    
    /**
     * Obtains the requested resource
     * @param database name of the database the resource belongs to
     * @param resource name of the resource we want to access
     * @param pk optional field which indicates the primary key column name
     * @param id the primary key value of the resource we want to access
     * @param ui the context of the request. Used to obtain the ordering and pagination parameters.
     * @return the resource if it's found, or a 404 if it's not.
     */
    public Response get(String database, String resource, String pk, String id, UriInfo ui);
    public Response getAll(String database, String resource, String pk, UriInfo ui);
    public Response find(String database, String resource, String col, String arg, UriInfo ui);
    public Response findBy(String database, String resource, String query, List<String> args, UriInfo ui);
    public Response insert(String database, String resource, String pk, String jsonRequest);
    public Response insert(String database, String resource, String pk, MultivaluedMap<String, String> formParams);
    public Response update(String database, String resource, String pk, String id, String jsonRequest);
    public Response delete(String database, String resource, String pk, String id);
    public Response storedProcedure(String database, String query, String jsonRequest);
    public Response getJongoStatistics();
    
}
