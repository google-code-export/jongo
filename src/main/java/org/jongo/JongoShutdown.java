package org.jongo;

import org.jongo.jdbc.JDBCExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoShutdown extends Thread{
    
    private static final Logger l = LoggerFactory.getLogger(JongoShutdown.class);
    
    public JongoShutdown(){
        super();
    }
    
    @Override
    public void run(){
        l.info("Shutting down Jongo");
        JDBCExecutor.shutdown();
    }
}
