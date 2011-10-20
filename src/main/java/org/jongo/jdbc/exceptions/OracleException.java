package org.jongo.jdbc.exceptions;

import java.sql.SQLException;

/**
 * Subclass of DatabaseException with specific knowledge of various Oracle sql error codes.
 * @author Jeff S Smith (jeff@SoftTechDesign.com, www.SoftTechDesign.com)
 */
public class OracleException extends JongoJDBCException
{
	/**
	 * Constructor for OracleException.
	 * @param s
	 * @param e
	 */
	public OracleException(String msg, SQLException e)
	{
		super(msg, e);
	}

	/**
	 * Constructor for OracleException.
	 * @param s
	 */
	public OracleException(String msg)
	{
		super(msg);
	}
    
    public OracleException(String msg, int e) {
        super(msg,e);
    }

	/**
	 * Was db exception caused by a data integrity violation
	 * @return true or false
	 */
    @Override
	public boolean isDataIntegrityViolation()
	{
		switch (sqlErrorCode)
		{
		case 1:
		case 1407: 
		case 1722: return(true); //1722 = invalid number
		default: return(false);
		}
	}

	/**
	 * Was db exception caused by a duplicate record (unique constraint) violation 
	 */
    @Override
	public boolean isUniqueConstraintViolation()
	{
		return(sqlErrorCode == 1);
	}

	/**
	 * Was db exception caused by bad sql grammer (a typo)
	 * return true or false
	 */
    @Override
	public boolean isBadSQLGrammar()
	{
		return(((sqlErrorCode >= 900) && (sqlErrorCode <= 999)));
	}

	/**
	 * Was db exception caused by referencing a non existent table or view
	 * @return true or false
	 */
    @Override
	public boolean isNonExistentTableOrViewOrCol()
	{
		switch (sqlErrorCode)
		{
		case 942:   //invalid table or view
		case 904: return(true);  //invalid col
		default : return(false);
		}
	}

	/** 
	 * Was db exception caused by referencing a invalid bind parameter name
	 * @return true or false
	 */
    @Override
	public boolean isInvalidBindVariableName()
	{
		return((sqlErrorCode == 1745) || (sqlErrorCode == 1006));
	}

	/**
	 * Was db exception caused by database being unavailable
	 * @return true or false
	 */
    @Override
	public boolean isDatabaseUnavailable()
	{
		return( ((sqlErrorCode >= 1033) && (sqlErrorCode <= 1035)) ||
				(sqlErrorCode == 1071) || (sqlErrorCode == 1089) ||
				(sqlErrorCode == 1092) || (sqlErrorCode == 1109));
	}

	/**
	 * Was db exception caused by a row lock error or some other sql querty timeout
	 * @return true or false
	 */
    @Override
	public boolean isRowlockOrTimedOut()
	{
		switch (sqlErrorCode)
		{
		case 104:
		case 1013:  //timed out
		case 2087:
		case 60: return(true);
		default: return(false);
		}
	}

	/** was db exception caused by a an unbound variable (parameter) 
	 * @return boolean
	 */
    @Override
	public boolean isVarParameterUnbound()
	{
		return(sqlErrorCode == 1008);
	}
}
