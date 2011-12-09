package org.jongo.rest.xstream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface JongoResponse {
    public String getResource();
    public Status getStatus();
    public boolean isSuccess();
    public String toJSON();
    public String toXML();
    public Response getResponse(final String format);
}
