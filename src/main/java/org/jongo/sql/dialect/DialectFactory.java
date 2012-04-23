package org.jongo.sql.dialect;

import org.jongo.enums.JDBCDriver;

/**
 * Factory method to generate SQL Dialects
 * @author Alejandro Ayuso 
 */
public class DialectFactory {
    
    /**
     * For a given driver, return the appropriate dialect.
     * @param driver a {@link org.jongo.enums.JDBCDriver}
     * @return a {@link org.jongo.sql.dialect.Dialect} for the driver.
     */
    public static Dialect getDialect(final JDBCDriver driver){
        Dialect dialect; 
        switch(driver){
            case HSQLDB_MEM:
            case HSQLDB_FILE:
                dialect = new HSQLDialect(); 
                break;
            
            case MSSQL_JTDS:
            case MSSQL: 
                dialect = new MSSQLDialect(); 
                break;
                
            case MySQL: dialect = new MySQLDialect(); break;
            case ORACLE: dialect = new OracleDialect(); break;
            case PostgreSQL: dialect = new PostgreSQLDialect(); break;
            default: dialect = new SQLDialect(); break;
        }
        return dialect;
        
    }
}
