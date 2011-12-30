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
package org.jongo.config;

import org.apache.commons.lang.StringUtils;
import org.jongo.enums.JDBCDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to describe a database configuration object.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class DatabaseConfiguration {
    
    private static final Logger l = LoggerFactory.getLogger(DatabaseConfiguration.class);
    
    private final String name;
    private final JDBCDriver driver;
    private final String user;
    private final String password;
    private final String url;

    public DatabaseConfiguration(String name, JDBCDriver driver, String user, String password, String url) {
        this.name = name;
        this.driver = driver;
        this.user = user;
        this.password = password;
        this.url = url;
    }

    public JDBCDriver getDriver() {
        return driver;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }
    
    public boolean isValid(){
        if(StringUtils.isBlank(name)){
            l.warn("Invalid database name. Check your configuration.");
            return false;
        }
        
        if(StringUtils.isBlank(user)){
            l.warn("Invalid database user. Check your configuration.");
            return false;
        }
        
        if(StringUtils.isBlank(password)){
            l.warn("Invalid database password. Check your configuration.");
            return false;
        }
        
        if(StringUtils.isBlank(url)){
            l.warn("Invalid database url. Check your configuration.");
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return "DatabaseConfiguration{" + "name=" + name + ", driver=" + driver + ", user=" + user + ", password=" + password + ", url=" + url + '}';
    }
    
    
}
