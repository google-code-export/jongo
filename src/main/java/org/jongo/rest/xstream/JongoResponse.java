package org.jongo.rest.xstream;

import com.thoughtworks.xstream.XStream;
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
    
    public String toXML(){
        XStream xStream = new XStream();
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.autodetectAnnotations(false);
        xStream.alias("response", JongoResponse.class);
        xStream.alias("row", RowResponse.class);
        xStream.registerConverter(new JongoMapConverter());
        xStream.aliasAttribute(RowResponse.class, "roi", "roi");
        xStream.omitField(JongoResponse.class, "sessionId");
        xStream.omitField(JongoResponse.class, "status");
        xStream.omitField(JongoResponse.class, "success");
        return xStream.toXML(this);
    }
    
    public String toJSON(){
        // I really tried to use XStream to generate the JSON, but it simply didn't do what I wanted.
        StringBuilder b = new StringBuilder("{\"response\":[");
        for(RowResponse row : rows){
            b.append("\"row\":");
            b.append(row.toJSON());
            b.append(",");
        }
        b.deleteCharAt(b.length() - 1);
        b.append("]}");
        return b.toString();
    }
    
    public Response getResponse(final String format){
        String response = (format.equalsIgnoreCase("json")) ? this.toJSON() : this.toXML();
        String media = (format.equalsIgnoreCase("json")) ? MediaType.APPLICATION_JSON : MediaType.APPLICATION_XML;
        return Response.status(this.status).entity(response).type(media).build();
    }
}
