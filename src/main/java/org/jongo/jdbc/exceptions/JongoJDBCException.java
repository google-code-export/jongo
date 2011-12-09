package org.jongo.jdbc.exceptions;

import java.sql.SQLException;
import javax.ws.rs.core.Response;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoResponse;

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
        JongoResponse error = new JongoError(null, getResponseStatus(), this.getMessage());
        return error.getResponse(format);
    }
}
