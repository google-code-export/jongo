package org.jongo.jdbc;

import org.jongo.enums.JDBCDriver;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class AbstractJDBCConnection {
    
    protected String url;
    protected String username;
    protected String password;
    protected JDBCDriver driver;
    
}
