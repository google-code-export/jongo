package org.jongo.sql.dialect;

import org.jongo.enums.JDBCDriver;

/**
 * 
 * @author Alejandro Ayuso 
 */
public class DialectFactory {
    
    public static Dialect getDialect(final JDBCDriver driver){
        Dialect dialect; 
        switch(driver){
            case HSQLDB: dialect = new HSQLDialect(); break;
            case MySQL: dialect = new MySQLDialect(); break;
            case ORACLE: dialect = new OracleDialect(); break;
            case PostgreSQL: dialect = new PostgreSQLDialect(); break;
            case MSSQL: dialect = new MSSQLDialect(); break;
            default: dialect = new SQLDialect(); break;
        }
        return dialect;
        
    }
}
