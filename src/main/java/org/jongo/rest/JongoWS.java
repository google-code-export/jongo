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

/**
 * Public RESTful webservice which allows CRUD operations on a given resource.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface JongoWS {
    
    public Response get(final String table, final String format);
    public Response get(final String table, final String format, final String id);
    public Response find(final String table, final String format, final String col, final String val);
    public Response findBy(final String table, final String format, final String query, final String value, final List<String> values);
    public Response insert(final String table, final String format, final String jsonRequest);
    public Response insert(final String table, final String format, final MultivaluedMap<String, String> formParams);
    public Response update(final String table, final String format, final String id, final String jsonRequest);
    public Response delete(final String table, final String format, final String id);
    public Response query(final String query, final String format, final List<String> arguments);
    
}
