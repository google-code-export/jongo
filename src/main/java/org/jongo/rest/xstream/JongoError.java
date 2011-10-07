package org.jongo.rest.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.jongo.enums.ErrorCode;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoError {
    private final String sessionId;
    private final boolean success = false;
    private final ErrorCode errorCode;
    private final String message;

    public JongoError(String sessionId, ErrorCode errorCode) {
        this.sessionId = sessionId;
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public JongoError(String sessionId, ErrorCode errorCode, String message) {
        this.sessionId = sessionId;
        this.errorCode = errorCode;
        this.message = message;
    }
    
    private static XStream initializeXStream(XStream xStream){
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.autodetectAnnotations(false);
        xStream.alias("response", JongoError.class);
        xStream.useAttributeFor(JongoError.class, "sessionId");
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
    
}
