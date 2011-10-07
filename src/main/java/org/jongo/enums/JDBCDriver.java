package org.jongo.enums;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public enum JDBCDriver {
    HSQLDB      ("org.hsqldb.jdbcDriver"),
    MySQL       ("com.mysql.jdbc.Driver"),
    PostgreSQL  ("org.postgresql.Driver"),
    ORACLE      ("oracle.jdbc.driver.OracleDriver");
    
    private final String name;
    
    private JDBCDriver(final String driverName){
        this.name = driverName;
    }
    
    public static boolean supported(final String driverName){
        for(JDBCDriver driver : JDBCDriver.values()){
            if(driver.name.equals(driverName)){
                return true;
            }
        }
        return false;
    }
    
    public static JDBCDriver driverOf(final String driverName){
        if(driverName == null)
            throw new IllegalArgumentException("Provide a driver");
        
        for(JDBCDriver driver : JDBCDriver.values()){
            if(driver.name.equals(driverName)){
                return driver;
            }
        }

        throw new IllegalArgumentException(driverName + " not supported");
    }
    
    public String getName(){
        return this.name;
    }
}
