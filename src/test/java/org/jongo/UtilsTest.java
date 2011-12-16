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
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.jdbc.connections.HSQLConnection;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class UtilsTest {
    
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
        HSQLConnection c = new HSQLConnection("jdbc", "k", "k");
        System.out.println(c.getSelectAllFromTableQuery("table", l, o));
    }
}
