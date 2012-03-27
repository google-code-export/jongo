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

package org.jongo.rest.xstream;

import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jongo.JongoUtils;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoSuccess implements JongoResponse{
    
    private final boolean success = true;
    private final Response.Status status;
    private final List<Row> rows;
    private final String resource;
    
    public JongoSuccess(String resource, List<Row> results, Response.Status status) {
        this.resource = resource;
        this.rows = results;
        this.status = status;
    }
    
    public JongoSuccess(String resource, List<Row> results) {
        this.resource = resource;
        this.rows = results;
        this.status = Response.Status.OK;
    }
    
    @Override
    public String toXML(){
        StringBuilder b = new StringBuilder("<response><success>");
        b.append(success);b.append("</success><resource>");
        b.append(resource);b.append("</resource><rows>");
        for(Row r : rows)
            b.append(r.toXML());
        b.append("</rows></response>");
        return b.toString();
    }
    
 
    @Override
    public String toJSON(){
        StringBuilder b = new StringBuilder("{");
        b.append("\"success\":");b.append(success);
        b.append(",\"cells\":[ "); //this last space is important!
        for(Row row : rows){
            b.append(row.toJSON());
            b.append(",");
        }
        b.deleteCharAt(b.length() - 1);
        b.append("]}");
        return b.toString();
    }
    
    @Override
    public Response getResponse(MediaType format) {
        String response = (format.isCompatible(MediaType.valueOf(MediaType.APPLICATION_XML))) ? this.toXML() : this.toJSON();
        return Response.status(this.status)
                .entity(response)
                .type(format)
                .header("Date", JongoUtils.getDateHeader())
                .header("Content-Count", rows.size())
                .header("Content-Location", resource)
                .build();
    }
    
    @Override
    public String getResource() {
        return resource;
    }

    public List<Row> getRows() {
        return rows;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
}
