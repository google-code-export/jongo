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

import org.jongo.jdbc.DynamicFinder;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class OracleConfigurationTest {
    
    OracleConfiguration instance = new OracleConfiguration("db", "sa", "sa", "jdbc:oracle:thin:@//demo1:3586/grrr");
    
    @BeforeClass
    public static void loadTestEnvironment(){
        System.setProperty("environment", "demo");
    }
    
    @AfterClass
    public static void unloadTestEnvironment(){
        System.setProperty("environment", "");
    }

    @Test
    public void testLoadDriver() {
        instance.loadDriver();
    }

    @Test
    public void testGetFirstRowQuery() {
        String q = instance.getFirstRowQuery("test");
        assertEquals("SELECT * FROM test WHERE rownum = 0", q);
        try{ instance.getFirstRowQuery("");     }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.getFirstRowQuery("    "); }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.getFirstRowQuery(null);   }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
    }

    @Test
    public void testGetSelectAllFromTableQuery() {
        String q = instance.getSelectAllFromTableQuery("test", new LimitParam(), new OrderParam());
        String r = "SELECT * FROM ( SELECT ROW_NUMBER() OVER (ORDER BY id ASC )AS ROW_NUMBER, test.* FROM test ) k WHERE ROW_NUMBER BETWEEN 25 AND 0";
        assertEquals(r, q);
        
        try{ instance.getSelectAllFromTableQuery("", new LimitParam(), new OrderParam());       }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.getSelectAllFromTableQuery("    ", new LimitParam(), new OrderParam());   }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.getSelectAllFromTableQuery(null, new LimitParam(), new OrderParam());     }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.getSelectAllFromTableQuery("test", null, new OrderParam());               }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.getSelectAllFromTableQuery("test", new LimitParam(), null);               }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        
        
        q = instance.getSelectAllFromTableQuery("test", "cid", new LimitParam(), new OrderParam());
        r = "SELECT * FROM ( SELECT ROW_NUMBER() OVER (ORDER BY id ASC )AS ROW_NUMBER, test.* FROM test WHERE cid= ? ) k WHERE ROW_NUMBER BETWEEN 25 AND 0";
        assertEquals(r, q);
    }

    @Test
    public void testGetListOfTablesQuery() {
        String q = instance.getListOfTablesQuery();
        assertEquals("SELECT TABLE_NAME FROM ALL_ALL_TABLES", q);
    }

    @Test
    public void testWrapDynamicFinderQuery() {
        String q = instance.wrapDynamicFinderQuery(new DynamicFinder("test", "findAllBy", "Name"), new LimitParam(), new OrderParam());
        String r = "SELECT * FROM ( SELECT ROW_NUMBER() OVER (ORDER BY id ASC )AS ROW_NUMBER, test.* FROM test WHERE  name = ? ) k WHERE ROW_NUMBER BETWEEN 25 AND 0";
        assertEquals(r, q);
        
        try{ instance.wrapDynamicFinderQuery(null, new LimitParam(), new OrderParam());                                 }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.wrapDynamicFinderQuery(new DynamicFinder("test", "findAllBy", "Name"), null, new OrderParam());   }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
        try{ instance.wrapDynamicFinderQuery(new DynamicFinder("test", "findAllBy", "Name"), new LimitParam(), null);   }catch(Exception e){assertTrue(e instanceof IllegalArgumentException);}
    }

    @Test
    public void testIsValid() {
        final String validUrl = "jdbc:oracle:thin:@//demo1:3586/grrr";
        assertTrue(new OracleConfiguration("db", "sa", "sa", validUrl).isValid());
        assertFalse(new OracleConfiguration("db", "sa", "sa", "jdbc:hsqldb://demo1:3586/grrr").isValid());
        assertFalse(new OracleConfiguration("db", "sa", "sa", "").isValid());
        assertFalse(new OracleConfiguration("db", "sa", "sa", null).isValid());
        assertFalse(new OracleConfiguration("", "sa", "sa", validUrl).isValid());
        assertFalse(new OracleConfiguration(null, "sa", "sa",validUrl).isValid());
        assertFalse(new OracleConfiguration("db", "", "sa", validUrl).isValid());
        assertFalse(new OracleConfiguration("db", null, "sa", validUrl).isValid());
        assertFalse(new OracleConfiguration("db", "sa", "", validUrl).isValid());
        assertFalse(new OracleConfiguration("db", "sa", null, validUrl).isValid());
    }
}
