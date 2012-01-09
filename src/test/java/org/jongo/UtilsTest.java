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

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.math.BigDecimal;
import java.text.ParseException;
import javax.ws.rs.core.MultivaluedMap;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jongo.config.AbstractDatabaseConfiguration;
import org.jongo.config.DatabaseConfiguration;
import org.jongo.enums.JDBCDriver;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.junit.Before;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class UtilsTest {
    
    @Before
    public void setUp(){
        System.setProperty("environment","demo");
    }
    
    @Test
    public void testIsDate() throws ParseException{
        DateTimeFormatter df = ISODateTimeFormat.date();
        DateTime date = df.parseDateTime("2011-01-19");
        assertEquals(date, JongoUtils.isDateTime("2011-01-19"));
        assertEquals(date, JongoUtils.isDateTime("20110119"));
        
        assertNull(JongoUtils.isDateTime("2011-19-01"));
        assertNull(JongoUtils.isDateTime("2011.01.19"));
        assertNull(JongoUtils.isDateTime("2011"));
        assertNull(JongoUtils.isDateTime(""));
        assertNull(JongoUtils.isDateTime(null));
    }
    
    @Test
    public void testIsDateTime(){
        DateTimeFormatter df = ISODateTimeFormat.dateTime();
        DateTime date = df.parseDateTime("2011-12-11T12:35:45.200+01:00");
        assertEquals(date, JongoUtils.isDateTime("2011-12-11T12:35:45.200+01:00"));
        assertNull(JongoUtils.isDateTime("2011-12-11 22:00:00"));
        assertNull(JongoUtils.isDateTime(""));
        assertNull(JongoUtils.isDateTime(null));
    }
    
    @Test
    public void testSplitCamelCase(){
        assertEquals(JongoUtils.splitCamelCase("nameIsNull"), "name Is Null");
        assertEquals(JongoUtils.splitCamelCase("name_idIsNull"), "name_id Is Null");
        assertEquals(JongoUtils.splitCamelCase("name_09IsNull"), "name_09 Is Null");
        assertEquals(JongoUtils.splitCamelCase("01_09IsNull"), "01_09 Is Null");
        assertEquals(JongoUtils.splitCamelCase("01IsNull"), "01 Is Null");
//        This is an invalid usage but the sql will break with this sort of query
//        assertEquals(JongoUtils.splitCamelCase("01*.Null"), "01 Is Null");
        assertEquals(JongoUtils.splitCamelCase(""), "");
    }
    
    @Test
    public void parseValue(){
        assertTrue(JongoUtils.parseValue("1") instanceof Integer);
        assertTrue(JongoUtils.parseValue("1.0") instanceof BigDecimal);
        assertTrue(JongoUtils.parseValue(" ") instanceof String);
        assertTrue(JongoUtils.parseValue("false") instanceof String);
        assertTrue(JongoUtils.parseValue("true") instanceof String);
        assertTrue(JongoUtils.parseValue("2011-12-11T12:35:45.200+01:00") instanceof java.sql.Date);
        assertTrue(JongoUtils.parseValue("2011-01-19") instanceof java.sql.Date);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testOrderParam(){
        MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
        assertEquals(OrderParam.valueOf(formParams).toString(), "{OrderParam:{column:\"id\", direction:\"ASC\"}}");
        formParams.add("dir", "KKK");
        OrderParam.valueOf(formParams).toString(); // throw Exception!
        formParams.add("dir", "DESC");
        assertEquals(OrderParam.valueOf(formParams).toString(), "{OrderParam:{column:\"id\", direction:\"ASC\"}}");
        formParams.add("sort", "kkk");
        assertEquals(OrderParam.valueOf(formParams).toString(), "{OrderParam:{column:\"kkk\", direction:\"DESC\"}}");
        formParams.remove("dir");
        assertEquals(OrderParam.valueOf(formParams).toString(), "{OrderParam:{column:\"kkk\", direction:\"ASC\"}}");
    }
    
    @Test
    public void testLimitParam(){
        MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
        LimitParam instance = LimitParam.valueOf(formParams);
        assertEquals(instance.getLimit(), new Integer(25));
        assertEquals(instance.getStart(), new Integer(0));
        formParams.putSingle("offset", "test");
        instance = LimitParam.valueOf(formParams);
        assertEquals(instance.getLimit(), new Integer(25));
        assertEquals(instance.getStart(), new Integer(0));
        formParams.putSingle("offset", "50");
        instance = LimitParam.valueOf(formParams);
        assertEquals(instance.getLimit(), new Integer(25));
        assertEquals(instance.getStart(), new Integer(0));
        formParams.putSingle("limit", "100");
        instance = LimitParam.valueOf(formParams);
        assertEquals(instance.getLimit(), new Integer(100));
        assertEquals(instance.getStart(), new Integer(50));
        formParams.remove("offset");
        instance = LimitParam.valueOf(formParams);
        assertEquals(instance.getLimit(), new Integer(100));
        assertEquals(instance.getStart(), new Integer(0));
    }
    
    @Test
    public void testSQL() throws Exception{
        LimitParam l = new LimitParam();
        OrderParam o = new OrderParam();
        DatabaseConfiguration c = AbstractDatabaseConfiguration.instanceOf("test1", JDBCDriver.HSQLDB, "k", "k", "jdbc");
        assertEquals(c.getSelectAllFromTableQuery("table", l, o), "SELECT * FROM table ORDER BY id ASC LIMIT 25 OFFSET 0");
        c = AbstractDatabaseConfiguration.instanceOf("test1", JDBCDriver.ORACLE, "k", "k", "jdbc");
        assertEquals(c.getSelectAllFromTableQuery("t", l, o), "SELECT * FROM ( SELECT ROW_NUMBER() OVER (ORDER BY null ASC )AS ROW_NUMBER, t .* FROM t ) k WHERE ROW_NUMBER <=25 AND ROW_NUMBER >=  0");
        assertEquals(c.getSelectAllFromTableQuery("t", "id", l, o), "SELECT * FROM ( SELECT ROW_NUMBER() OVER (ORDER BY null ASC )AS ROW_NUMBER, t .* FROM t WHERE id= ? ) k WHERE ROW_NUMBER <=25 AND ROW_NUMBER >=  0");
    }
}
