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
