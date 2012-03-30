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

import java.sql.SQLException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
@XmlRootElement(name="response")
public class JongoError implements JongoResponse {
    
    private String resource;
    private boolean success = false;
    private Integer status;
    private String message;
    private String sqlState;
    private Integer sqlCode;
    
    public JongoError(){}

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

    @Override
    public Response getResponse() {
        return Response.status(getStatus()).entity(this).build();
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

    public void setStatus(Integer status) {
        this.status = status;
    }
}
