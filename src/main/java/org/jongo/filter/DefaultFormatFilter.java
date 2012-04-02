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

package org.jongo.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.jongo.JongoUtils;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoHead;
import org.jongo.rest.xstream.JongoSuccess;
import org.jongo.rest.xstream.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class DefaultFormatFilter implements ContainerResponseFilter, JongoFormatFilter {

    private static final Logger l = LoggerFactory.getLogger(DefaultFormatFilter.class);

    @Override
    public ContainerResponse filter(ContainerRequest cr, ContainerResponse cr1) {
        final MediaType mime = getMediaTypeFromRequest(cr);
        final Response incResponse = cr1.getResponse();
        final Response formattedResponse = format(incResponse, mime);
        cr1.setResponse(formattedResponse);
        setHeadersToResponse(cr1, formattedResponse.getEntity());
        return cr1;
    }

    @Override
    public Response format(Response response, final MediaType mime) {
        final Object entity = response.getEntity();
        final Integer status = response.getStatus();
        if(entity instanceof JongoSuccess){
            return formatSuccessResponse((JongoSuccess)entity, mime, status);
        }else if(entity instanceof JongoError){
            return formatErrorResponse((JongoError)entity, mime, status);
        }else if(entity instanceof JongoHead){
            return formatHeadResponse((JongoHead)entity, mime, status);
        }else{
            return response;
        }
    }
    
    private void setHeadersToResponse(ContainerResponse cr1, final Object entity){
        if(entity != null){
            cr1.getHttpHeaders().add(HttpHeaders.DATE, JongoUtils.getDateHeader());
            cr1.getHttpHeaders().add("Content-MD5", JongoUtils.getMD5Base64(entity.toString()));
            cr1.getHttpHeaders().add(HttpHeaders.CONTENT_LENGTH, JongoUtils.getOctetLength(entity.toString()));
        }
    }
    
    private MediaType getMediaTypeFromRequest(final ContainerRequest cr){
        MediaType mime;
        final String rawMime = cr.getHeaderValue(HttpHeaders.ACCEPT);
        try{
            mime = MediaType.valueOf(rawMime);
        }catch(IllegalArgumentException e){
            l.warn("Failed to parse MIME in Accept header " + rawMime + ". Returning default application/json");
            mime = MediaType.valueOf(MediaType.APPLICATION_JSON);
        }
        return mime;
    }
    
    private boolean isXMLCompatible(final MediaType mime){
        if(mime.isWildcardType())
            return false;
        if(mime.isCompatible(MediaType.valueOf(MediaType.APPLICATION_XML)))
            return true;
        return false;
    }
    
    private Response formatSuccessResponse(final JongoSuccess response, final MediaType mime, final Integer status) {
        String res;
        l.debug("Formatting Success Response");
        if(isXMLCompatible(mime)){
            StringBuilder b = new StringBuilder("<response><success>");
            b.append(response.isSuccess());b.append("</success><resource>");
            b.append(response.getResource());b.append("</resource><rows>");
            for(Row r : response.getRows()){
                b.append("<row roi=\"");
                b.append(r.getRoi());
                b.append("\"><cells>");
                for (String key : r.getCells().keySet()) {
                    String val = r.getCells().get(key);
                    b.append("<");
                    b.append(key.toLowerCase());
                    b.append(">");
                    b.append(val);
                    b.append("</");
                    b.append(key.toLowerCase());
                    b.append(">");
                }
                b.append("</cells></row>");
            }
            b.append("</rows></response>");
            res = b.toString();
        }else{
            StringBuilder b = new StringBuilder("{");
            b.append("\"success\":");b.append(response.isSuccess());
            b.append(",\"cells\":[ "); //this last space is important!
            for(Row r : response.getRows()){
                List<String> args = new ArrayList<String>();
                for (String key : r.getCells().keySet()) {
                    String val = r.getCells().get(key);
                    if (StringUtils.isNumeric(val)) {
                        if (StringUtils.isWhitespace(val)) {
                            args.add("\"" + key.toLowerCase() + "\"" + ":" + "\"\"");
                        } else {
                            args.add("\"" + key.toLowerCase() + "\"" + ":" + val);
                        }
                    } else {
                        args.add("\"" + key.toLowerCase() + "\"" + ":" + "\"" + val + "\"");
                    }
                }

                b.append("{");
                b.append(StringUtils.join(args, ","));
                b.append("}");
                b.append(",");
            }
            b.deleteCharAt(b.length() - 1);
            b.append("]}");
            res = b.toString();
        }
        
        return Response.status(status)
                .entity(res)
                .type(mime)
                .header("Content-Count", response.getRows().size())
                .header(HttpHeaders.CONTENT_LOCATION, response.getResource())
                .build();
    }

    private Response formatErrorResponse(final JongoError response, final MediaType mime, final Integer status) {
        String res;
        l.debug("Formatting Error Response");
        if(isXMLCompatible(mime)){
            StringBuilder b = new StringBuilder("<response><success>");
            b.append(response.isSuccess());b.append("</success><message>");
            b.append(response.getMessage());b.append("</message>");
            if( response.getSqlCode() != null && response.getSqlState() != null){
                b.append("<sqlState>");b.append(response.getSqlState());b.append("</sqlState>");
                b.append("<sqlCode>");b.append(response.getSqlCode());b.append("</sqlCode>");
            }
            b.append("</response>");
            res = b.toString();
        }else{
            StringBuilder b = new StringBuilder("{");
            b.append("\"success\":");b.append(response.isSuccess());
            b.append(",\"message\":\"");b.append(response.getMessage());
            if( response.getSqlCode() != null && response.getSqlState() != null){
                b.append(",\"SQLState\":\"");b.append(response.getSqlState());
                b.append("\",\"SQLCode\":\"");b.append(response.getSqlCode());
            }
            b.append("\"}");
            res = b.toString();
        }
        return Response.status(status)
                .entity(res)
                .type(mime)
                .header("Content-Location", response.getResource())
                .build();
    }

    private Response formatHeadResponse(final JongoHead response, final MediaType mime, final Integer status) {
        final List<String> args = new ArrayList<String>();
        for(Row row : response.getRows()){
            final String columnname = row.getCells().get("columnName");
            final String columntype = row.getCells().get("columnType");
            final String columnsize = row.getCells().get("columnSize");
            StringBuilder b = new StringBuilder(columnname);
            b.append("=");
            b.append(columntype);
            b.append("(");
            b.append(columnsize);
            b.append(")");
            args.add(b.toString());
        }
        String res = StringUtils.join(args, ";");
        return Response.status(status)
                .type(mime)
                .header("Content-Location", response.getResource())
                .header(StringUtils.capitalize(response.getResource()), res)
                .build();
    }
}
