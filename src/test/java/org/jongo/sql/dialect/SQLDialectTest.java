package org.jongo.sql.dialect;

import junit.framework.Assert;
import org.jongo.JongoUtils;
import org.jongo.config.JongoConfiguration;
import org.jongo.demo.Demo;
import org.jongo.exceptions.StartupException;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.sql.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class SQLDialectTest {
    
    Table table = new Table("demo1", "a_table", "tableId");
    Dialect d = new SQLDialect();
    
    OrderParam o = new OrderParam(table);
    LimitParam l = new LimitParam();
    
    @BeforeClass
    public static void setUp() throws StartupException{
        System.setProperty("environment", "demo");
        JongoConfiguration configuration = JongoUtils.loadConfiguration();
        Demo.generateDemoDatabases(configuration.getDatabases());
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.setProperty("environment", "demo");
        JongoConfiguration configuration = JongoUtils.loadConfiguration();
        Demo.destroyDemoDatabases(configuration.getDatabases());
    }

    @Test
    public void testSelect() {
        doTest("SELECT * FROM demo1.a_table", new Select(table));
        
        doTest("SELECT * FROM demo1.a_table WHERE a_table.tableId=?", new Select(table).setValue("1"));
        
        doTest("SELECT * FROM demo1.a_table WHERE a_table.name=?", new Select(table).setValue("1").setColumn("name"));
        
        doTest("SELECT * FROM demo1.a_table WHERE a_table.name=? LIMIT 25 OFFSET 0",
                new Select(table).setValue("1").setColumn("name").setLimitParam(new LimitParam()));
        
        doTest("SELECT * FROM demo1.a_table WHERE a_table.tableId=? ORDER BY a_table.tableId ASC LIMIT 25 OFFSET 0",
                new Select(table).setValue("1").setLimitParam(l).setOrderParam(new OrderParam(table)));
    }
    
    @Test
    public void testDelete(){
        doTest("DELETE FROM demo1.a_table WHERE a_table.tableId=?", new Delete(table).setId("1"));
        doTest("DELETE FROM demo1.grrr WHERE grrr.id=?", new Delete(new Table("demo1", "grrr")).setId("1"));
    }
    
    @Test
    public void testInsert(){
        doTest("INSERT INTO demo1.a_table (name,age) VALUES (?,?)", new Insert(table).addColumn("name", "foo bar").addColumn("age", "50"));
        doTest("INSERT INTO demo1.a_table", new Insert(table));
    }
    
    @Test
    public void testUpdate(){
        doTest("UPDATE demo1.a_table SET name=?,age=? WHERE a_table.tableId=?", new Update(table).setId("1").addColumn("name", "foo bar").addColumn("age", "50"));
        doTest("UPDATE demo1.grrr SET name=? WHERE grrr.id=?", new Update(new Table("demo1","grrr")).setId("1").addColumn("name", "foo bar"));
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
