package org.jongo.sql.dialect;

import junit.framework.Assert;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.sql.*;
import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso
 */
public class SQLDialectTest {
    
    Table table = new Table("demo1", "a_table", "tableId");
    Dialect d;
    
    OrderParam o = new OrderParam(table);
    LimitParam l = new LimitParam();
    
    public SQLDialectTest(){
        d = new SQLDialect();
    }
    
    @Test
    public void testSelect() {
        doTest("SELECT * FROM a_table", new Select(table));
        
        doTest("SELECT * FROM a_table WHERE tableId=?", new Select(table).setValue("1"));
        
        doTest("SELECT * FROM a_table WHERE name=?", new Select(table).setValue("1").setColumn("name"));
        
        doTest("SELECT * FROM a_table WHERE name=? LIMIT 25 OFFSET 0",
                new Select(table).setValue("1").setColumn("name").setLimitParam(new LimitParam()));
        
        doTest("SELECT * FROM a_table WHERE tableId=? ORDER BY tableId ASC LIMIT 25 OFFSET 0",
                new Select(table).setValue("1").setLimitParam(l).setOrderParam(new OrderParam(table)));
    }
    
    @Test
    public void testDelete(){
        doTest("DELETE FROM a_table WHERE tableId=?", new Delete(table).setId("1"));
        doTest("DELETE FROM grrr WHERE id=?", new Delete(new Table("demo1", "grrr")).setId("1"));
    }
    
    @Test
    public void testInsert(){
        doTest("INSERT INTO a_table (name,age) VALUES (?,?)", new Insert(table).addColumn("name", "foo bar").addColumn("age", "50"));
    }
    
    @Test
    public void testUpdate(){
        doTest("UPDATE a_table SET name=?,age=? WHERE tableId=?", new Update(table).setId("1").addColumn("name", "foo bar").addColumn("age", "50"));
        doTest("UPDATE grrr SET name=? WHERE id=?", new Update(new Table("demo1","grrr")).setId("1").addColumn("name", "foo bar"));
    }
    
    public void doTest(String expected, Object obj){
        if(obj instanceof Select){
            Assert.assertEquals(expected, d.toStatementString((Select)obj));
        }else if(obj instanceof Delete){
            Assert.assertEquals(expected, d.toStatementString((Delete)obj));
        }else if(obj instanceof Insert){
            Assert.assertEquals(expected, d.toStatementString((Insert)obj));
        }else if(obj instanceof Update){
            Assert.assertEquals(expected, d.toStatementString((Update)obj));
        }
    }
}
