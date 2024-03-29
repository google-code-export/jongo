/**
 * Copyright (C) 2011, 2012 Alejandro Ayuso
 *
 * This file is part of Jongo.
 * Jongo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * Jongo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jongo.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jongo;

import org.jongo.jdbc.JDBCExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a JDBCExecutor.shutdown() in an independent thread.
 * @author Alejandro Ayuso 
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
