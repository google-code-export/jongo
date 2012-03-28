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
package org.jongo.config.impl;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class HSQLDBConfigurationTest {
    
    HSQLDBConfiguration instance = new HSQLDBConfiguration("db", "sa", "sa", "jdbc:hsqldb:mem:demo1");

    @Test
    public void testLoadDriver() {
        instance.loadDriver();
    }

    @Test
    public void testGetFirstRowQuery() {
        String q = instance.getFirstRowQuery("test");
        assertEquals("SELECT * FROM test LIMIT 1", q);
        try{ instance.getFirstRowQuery("");     }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.getFirstRowQuery("    "); }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.getFirstRowQuery(null);   }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
    }

    @Test
    public void testGetListOfTablesQuery() {
        String q = instance.getListOfTablesQuery();
        assertEquals("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE table_type = 'TABLE'", q);
    }
    
    @Test
    public void isValid(){
        final String validUrl = "jdbc:hsqldb:mem:demo1";
        assertTrue(new HSQLDBConfiguration("db", "sa", "sa", validUrl).isValid());
        assertFalse(new HSQLDBConfiguration("db", "sa", "sa", "http:hsqldb:mem:demo1").isValid());
        assertFalse(new HSQLDBConfiguration("db", "sa", "sa", "").isValid());
        assertFalse(new HSQLDBConfiguration("db", "sa", "sa", null).isValid());
        assertFalse(new HSQLDBConfiguration("", "sa", "sa", validUrl).isValid());
        assertFalse(new HSQLDBConfiguration(null, "sa", "sa", validUrl).isValid());
        assertFalse(new HSQLDBConfiguration("db", "", "sa", validUrl).isValid());
        assertFalse(new HSQLDBConfiguration("db", null, "sa", validUrl).isValid());
        assertFalse(new HSQLDBConfiguration("db", "sa", "", validUrl).isValid());
        assertFalse(new HSQLDBConfiguration("db", "sa", null, validUrl).isValid());
    }
}
