package org.jongo.rest.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoError {
    private final String resource;
    private final boolean success = false;
    private final Response.Status errorCode;
    private final String message;

    public JongoError(String resource, Response.Status errorCode) {
        this.resource = resource;
        this.errorCode = errorCode;
        this.message = errorCode.getReasonPhrase();
    }

    public JongoError(String resource, Response.Status errorCode, String message) {
        this.resource = resource;
        this.errorCode = errorCode;
        this.message = message;
    }
    
    private static XStream initializeXStream(XStream xStream){
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.autodetectAnnotations(false);
        xStream.alias("response", JongoError.class);
        return xStream;
    }
    
    public String toXML(){
        XStream xStream = new XStream();
        xStream = initializeXStream(xStream);
        return xStream.toXML(this);
    }
    
    public String toJSON(){
        XStream xStream = new XStream(new JettisonMappedXmlDriver());
        xStream = initializeXStream(xStream);
        return xStream.toXML(this);
    }
    
    public Response getResponse(final String format){
        String response = (format.equalsIgnoreCase("json")) ? this.toJSON() : this.toXML();
        String media = (format.equalsIgnoreCase("json")) ? MediaType.APPLICATION_JSON : MediaType.APPLICATION_XML;
        return Response.status(this.errorCode).entity(response).type(media).build();
    }
}
