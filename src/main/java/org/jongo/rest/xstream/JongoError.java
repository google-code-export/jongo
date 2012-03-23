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
import java.sql.SQLException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jongo.JongoUtils;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoError implements JongoResponse {
    
    private static final XStream xStream = initializeXStream();
    
    private final String resource;
    private final boolean success = false;
    private final Integer status;
    private final String message;
    private final String sqlState;
    private final Integer sqlCode;

    public JongoError(String resource, Response.Status status) {
        this.resource = resource;
        this.status = status.getStatusCode();
        this.message = status.getReasonPhrase();
        this.sqlState = null;
        this.sqlCode = null;
    }

    public JongoError(String resource, Integer errorCode, String message) {
        this.resource = resource;
        this.status = errorCode;
        this.message = message;
        this.sqlState = null;
        this.sqlCode = null;
    }
    
    public JongoError(String resource, Response.Status status, String message) {
        this.resource = resource;
        this.status = status.getStatusCode();
        this.message = message;
        this.sqlState = null;
        this.sqlCode = null;
    }
    
    public JongoError(final String resource, final SQLException ex){
        this.resource = resource;
        this.status = 400;
        this.message = ex.getMessage();
        this.sqlState = ex.getSQLState();
        this.sqlCode = ex.getErrorCode();
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
        b.append(",\"message\":\"");b.append(message);
        if( sqlCode != null && sqlState != null){
            b.append(",\"SQLState\":\"");b.append(sqlState);
            b.append("\",\"SQLCode\":\"");b.append(sqlCode);
        }
        b.append("\"}");
        return b.toString();
    }
    
    @Override
    public Response getResponse(MediaType format) {
        String response = (format.isCompatible(MediaType.valueOf(MediaType.APPLICATION_XML))) ? this.toXML() : this.toJSON();
        String md5sum = JongoUtils.getMD5Base64(response);
        Integer length = JongoUtils.getOctetLength(response);
        return Response.status(this.status)
                .entity(response)
                .type(format)
                .header("Date", JongoUtils.getDateHeader())
                .header("Content-MD5", md5sum)
                .header("Content-Length", length)
                .header("Content-Location", resource)
                .build();
    }

    @Override
    public Status getStatus() {
        return Response.Status.fromStatusCode(status);
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

    public Integer getSqlCode() {
        return sqlCode;
    }

    public String getSqlState() {
        return sqlState;
    }
}
