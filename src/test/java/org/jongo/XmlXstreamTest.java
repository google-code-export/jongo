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

import com.thoughtworks.xstream.XStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.jongo.mocks.JongoMapConverter;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoSuccess;
import org.jongo.rest.xstream.Row;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class XmlXstreamTest {
    
    public XmlXstreamTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Test
    public void testSuccessToXML(){
        Map<String, String> m1 = new HashMap<String, String>();
        List<Row> rows = new ArrayList<Row>();
        
        m1.put("id", "1");m1.put("name", "test1");m1.put("age", "56");
        rows.add(new Row(1, m1));
        
        m1 = new HashMap<String, String>();
        m1.put("id", "2");m1.put("name", "test2");m1.put("age", "526");
        
        rows.add(new Row(1, m1));
        
        JongoSuccess s = new JongoSuccess("test", rows);
        System.out.println(s.toXML());
        s = successFromXML(s.toXML());
        Assert.assertTrue(s.isSuccess());
    }
    
    @Test
    public void testErrorToXML(){
        JongoError s = new JongoError("grrr", 500, "grrrr error");
        System.out.println(s.toXML());
        s = errorFromXML(s.toXML());
        Assert.assertFalse(s.isSuccess());
        s = new JongoError("grrr", new SQLException("grrr", "GR101", 54333));
        System.out.println(s.toXML());
        s = errorFromXML(s.toXML());
        Assert.assertFalse(s.isSuccess());
    }
    
    public static JongoSuccess successFromXML(final String xml){
        XStream xStreamInstance = new XStream();
        xStreamInstance.setMode(XStream.NO_REFERENCES);
        xStreamInstance.autodetectAnnotations(false);
        xStreamInstance.alias("response", JongoSuccess.class);
        xStreamInstance.alias("row", Row.class);
        xStreamInstance.registerConverter(new JongoMapConverter());
        xStreamInstance.aliasAttribute(Row.class, "roi", "roi");
        return (JongoSuccess)xStreamInstance.fromXML(xml);
    }
    
    public static JongoError errorFromXML(final String xml){
        XStream xStreamInstance = new XStream();
        xStreamInstance.setMode(XStream.NO_REFERENCES);
        xStreamInstance.autodetectAnnotations(false);
        xStreamInstance.alias("response", JongoError.class);
        return (JongoError)xStreamInstance.fromXML(xml);
    }
}