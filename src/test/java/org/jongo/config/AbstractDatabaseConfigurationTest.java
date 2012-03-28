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

import org.jongo.enums.JDBCDriver;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class AbstractDatabaseConfigurationTest {
    
    public class AbstractDatabaseConfigurationImpl extends AbstractDatabaseConfiguration implements DatabaseConfiguration {

        public AbstractDatabaseConfigurationImpl() {
        }
        
        public AbstractDatabaseConfigurationImpl(String name, String user, String password, String url){
            this.name = name;
            this.driver = JDBCDriver.HSQLDB;
            this.username = user;
            this.password = password;
            this.url = url;
        }

        @Override
        public void loadDriver() {}

        @Override
        public String getListOfTablesQuery() {
            return "";
        }
    }
    
    AbstractDatabaseConfigurationImpl instance = new AbstractDatabaseConfigurationImpl("db", "sa", "sa", "jdbc:hsqldb:mem:demo1");
    
    @Test
    public void testGetFirstRowQuery() {
        String q = instance.getFirstRowQuery("test");
        assertEquals("SELECT * FROM test FETCH FIRST 1 ROW ONLY", q);
        try{ instance.getFirstRowQuery("");     }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.getFirstRowQuery("    "); }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.getFirstRowQuery(null);   }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
    }

    @Test
    public void testGetDriver() {
        assertEquals(JDBCDriver.HSQLDB, instance.getDriver());
    }

    @Test
    public void testGetName() {
        assertEquals("db", instance.getName());
    }

    @Test
    public void testToString() {
        assertEquals("DatabaseConfiguration{name=db, driver=HSQLDB, user=sa, password=sa, url=jdbc:hsqldb:mem:demo1}", instance.toString());
    }

    
}
