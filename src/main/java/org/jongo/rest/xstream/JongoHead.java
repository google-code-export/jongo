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
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.jongo.JongoUtils;

/**
 * Jongo Response for HTTP HEAD requests for the resources metadata. Currently it only supports tables & views.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoHead implements JongoResponse {
    private final boolean success = true;
    private final Response.Status status;
    private final List<RowResponse> rows;
    private final String resource;
    
    private static final XStream xStream = initializeXStream(); 
    
    public JongoHead(String resource, List<RowResponse> results, Response.Status status) {
        this.resource = resource;
        this.rows = results;
        this.status = status;
    }
    
    public JongoHead(String resource, List<RowResponse> results) {
        this.resource = resource;
        this.rows = results;
        this.status = Response.Status.OK;
    }
    
    @Override
    public String toXML(){
        return xStream.toXML(this);
    }
    
    public static JongoSuccess fromXML(final String xml){
        return (JongoSuccess)xStream.fromXML(xml);
    }
    
    @Override
    public String toJSON(){
        // I really tried to use XStream to generate the JSON, but it simply didn't do what I wanted. 
        // It kept adding the response as an object and I want an array.
        StringBuilder b = new StringBuilder("{");
        b.append("\"success\":");b.append(success);
        b.append(",\"response\":[ "); //this last space is important!
        for(RowResponse row : rows){
            b.append(row.toJSON());
            b.append(",");
        }
        b.deleteCharAt(b.length() - 1);
        b.append("]}");
        return b.toString();
    }
    
    @Override
    public Response getResponse(final String format){
        String response = (format.equalsIgnoreCase("json")) ? this.toJSON() : this.toXML();
        String media = (format.equalsIgnoreCase("json")) ? MediaType.APPLICATION_JSON : MediaType.APPLICATION_XML;
        return Response.status(this.status)
                .entity(response) // leave this in case the client can handle HEAD with content.
                .type(media)
                .header("Date", JongoUtils.getDateHeader())
                .header("Content-Location", resource)
                .header(StringUtils.capitalize(resource), this.toHeader())
                .build();
    }
    
    private static XStream initializeXStream(){
        XStream xStreamInstance = new XStream();
        xStreamInstance.setMode(XStream.NO_REFERENCES);
        xStreamInstance.autodetectAnnotations(false);
        xStreamInstance.alias("response", JongoSuccess.class);
        xStreamInstance.alias("row", RowResponse.class);
        xStreamInstance.registerConverter(new JongoMapConverter());
        xStreamInstance.aliasAttribute(RowResponse.class, "roi", "roi");
        return xStreamInstance;
    }

    @Override
    public String getResource() {
        return resource;
    }

    public List<RowResponse> getRows() {
        return rows;
    }

    @Override
    public Response.Status getStatus() {
        return status;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }
    
    private String toHeader(){
        final List<String> args = new ArrayList<String>();
        for(RowResponse row : rows){
            final String columnname = row.getColumns().get("columnName");
            final String columntype = row.getColumns().get("columnType");
            final String columnsize = row.getColumns().get("columnSize");
            StringBuilder b = new StringBuilder(columnname);
            b.append("=");
            b.append(columntype);
            b.append("(");
            b.append(columnsize);
            b.append(")");
            args.add(b.toString());
        }
        return StringUtils.join(args, ";");
    }
}
