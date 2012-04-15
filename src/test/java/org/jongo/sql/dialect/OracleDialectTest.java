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
package org.jongo.sql.dialect;

import junit.framework.Assert;
import org.jongo.jdbc.DynamicFinder;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.sql.*;
import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class OracleDialectTest extends SQLDialectTest {
    
    public OracleDialectTest() {
        d = new OracleDialect();
    }

    @Test
    @Override
    public void testSelect() {
        doTest("SELECT a_table.* FROM demo1.a_table", new Select(table));
        
        doTest("SELECT a_table.* FROM demo1.a_table WHERE a_table.tableId=?", new Select(table).setValue("1"));
        
        doTest("SELECT a_table.* FROM demo1.a_table WHERE a_table.name=?", new Select(table).setValue("1").setColumn("name"));
        
        doTest("SELECT a_table.* FROM demo1.a_table WHERE a_table.tableId=? ORDER BY a_table.tableId ASC", new Select(table).setValue("1").setOrderParam(new OrderParam(table)));
        
        doTest("SELECT * FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY a_table.tableId ) AS ROW_NUMBER, a_table.* FROM demo1.a_table WHERE a_table.name=?) WHERE ROW_NUMBER BETWEEN 0 AND 25",
                new Select(table).setValue("1").setColumn("name").setLimitParam(new LimitParam()));
        
        doTest("SELECT * FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY a_table.name DESC ) AS ROW_NUMBER, a_table.* FROM demo1.a_table WHERE a_table.tableId=?) WHERE ROW_NUMBER BETWEEN 0 AND 25",
                new Select(table).setValue("1").setLimitParam(l).setOrderParam(new OrderParam("name", "DESC")));
    }

    @Test
    @Override
    public void testDelete() {
        doTest("DELETE FROM demo1.a_table WHERE a_table.tableId=?", new Delete(table).setId("1"));
        doTest("DELETE FROM demo1.grrr WHERE grrr.id=?", new Delete(new Table("demo1", "grrr")).setId("1"));
    }

    @Test
    @Override
    public void testInsert(){
        doTest("INSERT INTO demo1.a_table (name,age) VALUES (?,?)", new Insert(table).addColumn("name", "foo bar").addColumn("age", "50"));
    }
    
    @Test
    @Override
    public void testUpdate(){
        doTest("UPDATE demo1.a_table SET a_table.name=?,a_table.age=? WHERE a_table.tableId=?", new Update(table).setId("1").addColumn("name", "foo bar").addColumn("age", "50"));
        doTest("UPDATE demo1.grrr SET grrr.name=? WHERE grrr.id=?", new Update(new Table("demo1","grrr")).setId("1").addColumn("name", "foo bar"));
    }
    
    @Test
    public void testDynamicFinders(){
        System.out.println(d.toStatementString(new DynamicFinder("test", "findAllBy", "Name"), l,o));
        Assert.assertEquals("SELECT * FROM ( SELECT ROW_NUMBER() OVER ( ORDER BY test.tableId ASC ) AS ROW_NUMBER, test.* FROM test WHERE  name = ? ) WHERE ROW_NUMBER BETWEEN 0 AND 25",
                d.toStatementString(new DynamicFinder("test", "findAllBy", "Name"), l,o));
    }
}
