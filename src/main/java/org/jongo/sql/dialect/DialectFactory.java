package org.jongo.sql.dialect;

import java.util.EnumSet;
import java.util.Set;
import org.jongo.enums.JDBCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso 
 */
public class DialectFactory {
    
    private static final Set<JDBCDriver> loadedDrivers = EnumSet.noneOf(JDBCDriver.class);
    
    private static final Logger l = LoggerFactory.getLogger(DialectFactory.class);
    
    public static Dialect getDialect(final JDBCDriver driver){
        Dialect dialect; 
        switch(driver){
            case HSQLDB: dialect = new HSQLDialect(); break;
            default: dialect = new SQLDialect(); break;
        }
//        loadDriver(driver);
        return dialect;
        
    }
    
//    private static void loadDriver(JDBCDriver driver) {
//        if(!loadedDrivers.contains(driver)){
//            l.debug("Loading Driver " + driver.getName());
//            try {
//                Class.forName(driver.getName());
//                loadedDrivers.add(driver);
//            } catch (ClassNotFoundException ex) {
//                l.error("Unable to load driver. Add the JDBC Connector jar to the lib folder");
//            }
//        }
//    }
    
}
