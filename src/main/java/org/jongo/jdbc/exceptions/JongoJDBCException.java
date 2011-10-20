package org.jongo.jdbc.exceptions;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import java.sql.SQLException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * DatabaseException denotes a generic runtime data access (SQL) exception. By declaring the 
 * exception as a descendant of RuntimeException, the jdbc framework give the programmer the option
 * of whether or not to catch the exception.
 * @author Jeff Smith (jeff@SoftTechDesign.com, www.SoftTechDesign.com)
 * @author Paolo Orru (paolo.orru@gmail.com) modified to add PostgreSQL support
 */
public abstract class JongoJDBCException extends Exception {
    
    public static final int ILLEGAL_READ_CODE = -2;
    public static final int ILLEGAL_WRITE_CODE = -3;
    public static final int ILLEGAL_ACCESS_CODE = -4;
    
	protected int sqlErrorCode = 0;
	protected String sqlState = null;
    
	public abstract boolean isDataIntegrityViolation();
	public abstract boolean isUniqueConstraintViolation();
	public abstract boolean isBadSQLGrammar();
	public abstract boolean isNonExistentTableOrViewOrCol();
	public abstract boolean isInvalidBindVariableName();
	public abstract boolean isDatabaseUnavailable();
	public abstract boolean isRowlockOrTimedOut();
	public abstract boolean isVarParameterUnbound();
    
    public JongoJDBCException(String msg){
        super(msg);
        sqlErrorCode = -1;
    }
    
    public JongoJDBCException(String msg, SQLException e){
		super(msg);
		sqlErrorCode = e.getErrorCode();
		sqlState = e.getSQLState (); 
	}
    
    public JongoJDBCException(String msg, int sqlErrorCode){
		super(msg);
		this.sqlErrorCode = sqlErrorCode;
		sqlState = null;
	}
    
	public int getSqlErrorCode(){
		return(sqlErrorCode);
	}

	public String getSqlState(){
		return(sqlState);
	}
    
    public boolean isIllegalAccess(){
        return this.sqlErrorCode == ILLEGAL_ACCESS_CODE;
    }
    
    public boolean isWriteAccess(){
        return this.sqlErrorCode == ILLEGAL_WRITE_CODE;
    }
    
    public boolean isReadAccess(){
        return this.sqlErrorCode == ILLEGAL_READ_CODE;
    }
    
    private static XStream initializeXStream(XStream xStream){
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.autodetectAnnotations(false);
//        xStream.alias("response", JongoError.class);
//        xStream.useAttributeFor(JongoError.class, "sessionId");
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
    
    private Response.Status getResponseStatus(){
        if(isDataIntegrityViolation() ||
                isBadSQLGrammar() ||
                isUniqueConstraintViolation() ||
                isVarParameterUnbound()){
            return Response.Status.BAD_REQUEST;
        }else if(isIllegalAccess() || isReadAccess() || isWriteAccess()){
            return Response.Status.FORBIDDEN;
        }else if(isNonExistentTableOrViewOrCol()){
            return Response.Status.NOT_FOUND;
        }else{
            return Response.Status.INTERNAL_SERVER_ERROR;
        }
    }
    
    public Response getResponse(final String format){
        String response = (format.equalsIgnoreCase("json")) ? this.toJSON() : this.toXML();
        String media = (format.equalsIgnoreCase("json")) ? MediaType.APPLICATION_JSON : MediaType.APPLICATION_XML;
        return Response.status(getResponseStatus()).entity(response).type(media).build();
    }
}
