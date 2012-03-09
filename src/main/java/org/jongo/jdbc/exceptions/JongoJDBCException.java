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

package org.jongo.jdbc.exceptions;

import java.sql.SQLException;
import javax.ws.rs.core.Response;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoResponse;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
@Deprecated
public abstract class JongoJDBCException extends SQLException {
    
    protected String database;
    
    /**
     * Couldn't connect to the desired database or schema because
     * of different reasons (invalid authentication, no connection, etc).
     * @return 
     */
    public abstract boolean isBadGateway();
    
    /**
     * Couldn't connect to the desired database or schema because of
     * a timeout from the datasource.
     * @return 
     */
    public abstract boolean isGatewayTimeout();
    
    /**
     * The data provided in the request is wrong and the database
     * couldn't work with it.
     * @return 
     */
    public abstract boolean isBadRequest();
    
    /**
     * The request was a legal request, but the server is refusing to respond to it.
     * @return 
     */
    public abstract boolean isForbidden();
    
    /**
     * The requested resource (schema, table, view, stored procedure)
     * could not be found but may be available again in the future.
     * @return 
     */
    public abstract boolean isNotFound();
    
    public String getDatabase(){
        return this.database;
    }
    
    public void setDatabase(final String database){
        this.database = database;
    }
    
    public Response.Status getHTTPStatus() {
        if(this.isBadRequest()){
            return Response.Status.BAD_REQUEST;
        }else if(this.isForbidden()){
            return Response.Status.FORBIDDEN;
        }else if(this.isNotFound()){
            return Response.Status.NOT_FOUND;
        }else if(this.isBadGateway()){
            return Response.Status.INTERNAL_SERVER_ERROR;
        }else if(this.isGatewayTimeout()){
            return Response.Status.SERVICE_UNAVAILABLE;
        }else{
            return Response.Status.INTERNAL_SERVER_ERROR;
        }
    }
}