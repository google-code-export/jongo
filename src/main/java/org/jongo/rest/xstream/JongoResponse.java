package org.jongo.rest.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoResponse {
    
    private final String sessionId;
    private final boolean success = true;
    private final Response.Status status;
    private final List<RowResponse> rows;
    
    public JongoResponse(String sessionId, List<RowResponse> results, Response.Status status) {
        this.sessionId = sessionId;
        this.rows = results;
        this.status = status;
    }
    
    public JongoResponse(String sessionId, List<RowResponse> results) {
        this.sessionId = sessionId;
        this.rows = results;
        this.status = Response.Status.OK;
    }
    
    private static XStream initializeXStream(XStream xStream){
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.autodetectAnnotations(false);
        
        // uncomment when using annotations (thread-unsafe)
        xStream.alias("response", JongoResponse.class);
        xStream.alias("row", RowResponse.class);
        xStream.registerConverter(new JongoMapConverter());
        xStream.useAttributeFor(RowResponse.class, "roi");
        xStream.useAttributeFor(JongoResponse.class, "sessionId");
        xStream.omitField(JongoResponse.class, "status");
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
        return Response.status(this.status).entity(response).type(media).build();
    }
}
