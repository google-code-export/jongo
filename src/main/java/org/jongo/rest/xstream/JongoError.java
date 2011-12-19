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

import com.thoughtworks.xstream.XStream;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoError implements JongoResponse {
    
    private static final XStream xStream = initializeXStream(); 
    
    private final String resource;
    private final boolean success = false;
    private final Response.Status status;
    private final String message;

    public JongoError(String resource, Response.Status status) {
        this.resource = resource;
        this.status = status;
        this.message = status.getReasonPhrase();
    }

    public JongoError(String resource, Response.Status errorCode, String message) {
        this.resource = resource;
        this.status = errorCode;
        this.message = message;
    }
    
    private static XStream initializeXStream(){
        XStream xStreamInstance = new XStream();
        xStreamInstance.setMode(XStream.NO_REFERENCES);
        xStreamInstance.autodetectAnnotations(false);
        xStreamInstance.alias("response", JongoError.class);
        return xStreamInstance;
    }
    
    @Override
    public String toXML(){
        return xStream.toXML(this);
    }
    
    public static JongoError fromXML(final String xml){
        return (JongoError)xStream.fromXML(xml);
    }
    
    @Override
    public String toJSON(){
        StringBuilder b = new StringBuilder("{");
        b.append("\"success\":");b.append(success);
        b.append(",\"resource\":\"");b.append(resource);
        b.append("\",\"error\":\"");b.append(status.name());
        b.append("\",\"code\":");b.append(status.getStatusCode());
        b.append(",\"message\":\"");b.append(message);
        b.append("\"}");
        return b.toString();
    }
    
    @Override
    public Response getResponse(final String format){
        String response = (format.equalsIgnoreCase("json")) ? this.toJSON() : this.toXML();
        String media = (format.equalsIgnoreCase("json")) ? MediaType.APPLICATION_JSON : MediaType.APPLICATION_XML;
        return Response.status(this.status).entity(response).type(media).build();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getResource() {
        return resource;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
}
