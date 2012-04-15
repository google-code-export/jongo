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
