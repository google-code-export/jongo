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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jongo.config.JongoConfiguration;
import org.jongo.demo.Demo;
import org.jongo.exceptions.JongoBadRequestException;
import org.jongo.exceptions.StartupException;
import org.jongo.jdbc.JDBCExecutor;
import org.jongo.jdbc.QueryParams;
import org.jongo.jdbc.StoredProcedureParam;
import org.jongo.mocks.DummyQueryParamsFactory;
import org.jongo.mocks.UserMock;
import org.jongo.rest.xstream.Row;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JDBCExecutorTest {

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
    public void testGet() throws SQLException{
        QueryParams q = DummyQueryParamsFactory.getUser();
        q.setId("0");
        List<Row> rs = JDBCExecutor.get(q);
        Row r = rs.get(0);
        assertEquals("30", r.getCells().get("AGE"));
        q.setId("1");
        rs = JDBCExecutor.get(q);
        r = rs.get(0);
        assertEquals("33", r.getCells().get("AGE"));
        q.setId("");
        rs = JDBCExecutor.get(q);
        assertEquals(2, rs.size());
    }
    
    @Test
    public void testAll() throws SQLException{
        QueryParams q = DummyQueryParamsFactory.getUser();
        List<UserMock> users = getTestValues();
        List<UserMock> createdusers = new ArrayList<UserMock>();
        
        for(UserMock u : users){
            q.setParams(u.toMap());
            int r = JDBCExecutor.insert(q);
            assertEquals(1, r);
            createdusers.add(u);
            q = DummyQueryParamsFactory.getUser();
            q.setIdField("name");
            List<Row> rs = JDBCExecutor.findByColumn(q, u.name);
            Row row = rs.get(0);
            assertEquals(String.valueOf(u.age), row.getCells().get("AGE"));
            q = DummyQueryParamsFactory.getUser();
            q.setId(row.getCells().get("ID"));
            q.setParam("age", "99");
            q.setParam("credit", "1.99");
            rs = JDBCExecutor.update(q);
            row = rs.get(0);
            assertEquals("99", row.getCells().get("AGE"));
            assertEquals(u.name, row.getCells().get("NAME"));
            q = DummyQueryParamsFactory.getUser();
            q.setId(row.getCells().get("ID"));
            r = JDBCExecutor.delete(q);
            assertEquals(1, r);
        }
        
    }
    
    @Test
    public void testInsert() throws SQLException{
        //test for empty QueryParams
        QueryParams q = new QueryParams();
        try{
            JDBCExecutor.insert(q);
        }catch (IllegalArgumentException e){
            assertNotNull(e.getMessage());
        }
        
        //test for empty parameters
        q = DummyQueryParamsFactory.getUser();
        try{
            JDBCExecutor.insert(q);
        }catch (IllegalArgumentException e){
            assertNotNull(e.getMessage());
        }
        Map<String, String> params = UserMock.getRandomInstance().toMap();
        q.setParams(params);
        int r = JDBCExecutor.insert(q);
        assertEquals(1, r); //only one registry added for success
        
        //test if one of the params is null
        params = UserMock.getRandomInstance().toMap();
        params.put("age", null);
        q.setParams(params);
        r = JDBCExecutor.insert(q);
        assertEquals(1, r);
        
        //test if one of the params is empty
        params = UserMock.getRandomInstance().toMap();
        params.put("birthday", "");
        q.setParams(params);
        try{
            JDBCExecutor.insert(q);
        }catch (SQLException e){
            assertNotNull(e.getMessage());
        }
        
        //test with a readonly table
        q = DummyQueryParamsFactory.getMakerTable();
        q.setParam("name", "RO");
        q.setParam("realname", "Read Only");
        try{
            JDBCExecutor.insert(q);
        }catch (SQLException e){
            assertNotNull(e.getMessage());
        }
    }
    
    @Test
    public void testUpdate() throws SQLException{
        QueryParams q = DummyQueryParamsFactory.getUser();
        q.setId("0");
        List<Row> rs = JDBCExecutor.get(q);
        Row row = rs.get(0);
        assertEquals("foo", row.getCells().get("NAME"));
        q.setParam("name", "fooer");
        rs = JDBCExecutor.update(q);
        row = rs.get(0);
        assertEquals("fooer", row.getCells().get("NAME"));
        
        //test for empty QueryParams
        q = new QueryParams();
        try{
            JDBCExecutor.update(q);
        }catch (IllegalArgumentException e){
            assertNotNull(e.getMessage());
        }
        
        //test for empty parameters
        q = DummyQueryParamsFactory.getUser();
        try{
            JDBCExecutor.update(q);
        }catch (IllegalArgumentException e){
            assertNotNull(e.getMessage());
        }
        
        //test for null value
        q = DummyQueryParamsFactory.getUser();
        q.setId("0");
        q.setParam("age", null);
        rs = JDBCExecutor.update(q);
        row = rs.get(0);
        assertEquals("fooer", row.getCells().get("NAME"));
        assertEquals(null, row.getCells().get("AGE"));
        
        //test for empty value
        q.setId("0");
        q.setParam("age", "35");
        q.setParam("name", "");
        rs = JDBCExecutor.update(q);
        row = rs.get(0);
        assertEquals("", row.getCells().get("NAME"));
        assertEquals("35", row.getCells().get("AGE"));
        
        //test with a readonly table
        q = DummyQueryParamsFactory.getMakerTable();
        q.setId("0");
        q.setParam("name", "RO");
        q.setParam("realname", "Read Only");
        try{
            JDBCExecutor.update(q);
        }catch (SQLException e){
            assertNotNull(e.getMessage());
        }
        
    }
    
    public List<UserMock> getTestValues(){
        List<UserMock> u1 = new ArrayList<UserMock>();
        u1.add(UserMock.getRandomInstance());
        u1.add(UserMock.getRandomInstance());
        u1.add(UserMock.getRandomInstance());
        return u1;
    }
            
    @Test
    public void testSimpleFunction() throws SQLException, JongoBadRequestException {
        List<StoredProcedureParam> params = new ArrayList<StoredProcedureParam>();
        List<Row> rows = JDBCExecutor.executeQuery("demo1", "simpleStoredProcedure", params);
        assertEquals(1, rows.size());
    }
    
    @Test
    public void testSimpleStoredProcedure() throws JongoBadRequestException, SQLException{
        List<StoredProcedureParam> params = new ArrayList<StoredProcedureParam>();
        QueryParams q = QueryParams.valueOf("demo1", "comments");
        List<Row> rs = JDBCExecutor.get(q);
        assertEquals(3, rs.size());
        
        params.add(new StoredProcedureParam("car_id", "1", false, 1, "INTEGER"));
        params.add(new StoredProcedureParam("comment", "grrrr asdsa  asd asd asda asdd )/(&&/($%/(&$=)/&/$·/(&", false, 2, "VARCHAR"));
        List<Row> rows = JDBCExecutor.executeQuery("demo1", "insert_comment", params);
        assertEquals(0, rows.size());
        
        rs = JDBCExecutor.get(q);
        assertEquals(4, rs.size());
    }
    
    @Test
    public void testComplexStoredProcedure() throws JongoBadRequestException, SQLException{
        List<StoredProcedureParam> params = new ArrayList<StoredProcedureParam>();
        params = new ArrayList<StoredProcedureParam>();
        params.add(new StoredProcedureParam("in_year", "2010", false, 1, "INTEGER"));
        params.add(new StoredProcedureParam("out_total", "", true, 2, "INTEGER"));
        List<Row> rows = JDBCExecutor.executeQuery("demo1", "get_year_sales", params);
        assertEquals(1, rows.size());
        assertEquals("12", rows.get(0).getCells().get("out_total"));
    }
}
