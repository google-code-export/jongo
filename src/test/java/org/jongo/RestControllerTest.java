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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import junit.framework.Assert;
import org.jongo.config.JongoConfiguration;
import org.jongo.demo.Demo;
import org.jongo.exceptions.StartupException;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.mocks.UserMock;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoHead;
import org.jongo.rest.xstream.JongoSuccess;
import org.jongo.rest.xstream.Row;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class RestControllerTest {
    
    private static final Logger l = LoggerFactory.getLogger(RestControllerTest.class);
    
    RestController controller = new RestController("demo1");
    LimitParam limit = new LimitParam();
    OrderParam order = new OrderParam();
    
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
    public void testGetDatabaseMetadata(){
        JongoSuccess r = (JongoSuccess)controller.getDatabaseMetadata();
        testSuccessResponse(r, Response.Status.OK, 8);
    }
    
    @Test
    public void testGetResourceMetadata(){
        JongoHead r = (JongoHead)controller.getResourceMetadata("user");
        Assert.assertEquals(Response.Status.OK, r.getStatus());
        Assert.assertTrue(r.isSuccess());
        Assert.assertEquals(6, r.getRows().size());
        
        JongoError err = (JongoError)controller.getResourceMetadata(null);
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.getResourceMetadata("");
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.getResourceMetadata("this table doesnt exists");
        testErrorResponse(err, Response.Status.BAD_REQUEST, "42501", new Integer(-5501));
    }
    
    @Test
    public void testReadResource(){
        
        JongoSuccess r = (JongoSuccess)controller.getResource("user", "id", "0", limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        r = (JongoSuccess)controller.getResource("user", "name", "foo", limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        r = (JongoSuccess)controller.getResource("user", "birthday", "1992-01-15", limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        r = (JongoSuccess)controller.getResource("user", "age", "33", limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        r = (JongoSuccess)controller.getResource("user", "credit", "32.5", limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        r = (JongoSuccess)controller.getResource("user", "id", "", limit, order);
        testSuccessResponse(r, Response.Status.OK, 2);
        
        r = (JongoSuccess)controller.getResource("user", "id", null, limit, order);
        testSuccessResponse(r, Response.Status.OK, 2);
        
        r = (JongoSuccess)controller.getResource("user", "", null, limit, order);
        testSuccessResponse(r, Response.Status.OK, 2);
        
        r = (JongoSuccess)controller.getResource("user", null, null, limit, order);
        testSuccessResponse(r, Response.Status.OK, 2);

        JongoError err = (JongoError)controller.getResource("", "id", "0", limit, order);
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.getResource(null, null, null, limit, order);
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        // fails if we try for a non-existing resource.
        err = (JongoError)controller.getResource("user", "id", "1999", limit, order);
        testErrorResponse(err, Response.Status.NOT_FOUND, null, null);
        
        // test a table with a custom column
        order.setColumn("cid");
        r = (JongoSuccess)controller.getResource("car", "cid", "1", limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        r = (JongoSuccess)controller.getResource("car", "transmission", "Automatic", limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
    }
    
    @Test
    public void testReadAllResources(){
        limit = new LimitParam();
        order = new OrderParam();
        JongoSuccess r = (JongoSuccess)controller.getAllResources("maker", limit, order);
        testSuccessResponse(r, Response.Status.OK, 25);
        
        JongoError err = (JongoError)controller.getAllResources("no_exists", limit, order);
        testErrorResponse(err, Response.Status.BAD_REQUEST, "42501", new Integer(-5501));
        
        r = (JongoSuccess)controller.getAllResources("empty", limit, order);
        testSuccessResponse(r, Response.Status.OK, 0);
    }
    
    @Test
    public void testFindByDynamicFinder(){
        testDynamicFinder("user", "findAllByAgeBetween", 2, "18", "99");
        testDynamicFinder("user", "findAllByBirthdayBetween", 1, "1992-01-01", "1992-12-31");
        
        order.setColumn("cid");
        testDynamicFinder("car", "findAllByFuelIsNull", 1);
        testDynamicFinder("car", "findAllByFuelIsNotNull", 2);
        
        order.setColumn("id");
        testDynamicFinder("user", "findAllByCreditGreaterThan", 1, "0");
        testDynamicFinder("user", "findAllByCreditGreaterThanEquals", 2, "0");
        testDynamicFinder("user", "findAllByCreditLessThanEquals", 1, "0");
        testDynamicFinder("sales_stats", "findAllByLast_updateBetween", 6, "2000-01-01T00:00:00.000Z", "2000-06-01T23:55:00.000Z");
        
        JongoError err = (JongoError)controller.findByDynamicFinder("user","findAllByCreditLessThan", Arrays.asList(new String [] {"0"}), limit, order);
        testErrorResponse(err, Response.Status.NOT_FOUND, null, null);
        
        err = (JongoError)controller.findByDynamicFinder("user","findAllByCreditLessThhhhan", Arrays.asList(new String [] {"0"}), limit, order);
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.findByDynamicFinder("user","", Arrays.asList(new String [] {"0"}), limit, order);
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
    }
    
    @Test
    public void testCreateResource(){
        UserMock newMock = UserMock.getRandomInstance();
        JongoSuccess r = (JongoSuccess)controller.insertResource("user", "id", newMock.toJSON());
        testSuccessResponse(r, Response.Status.CREATED, 1);
        
        r = (JongoSuccess)controller.getResource("user", "name", newMock.name, limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        newMock = UserMock.getRandomInstance();
        r = (JongoSuccess)controller.insertResource("user", "id", newMock.toMap());
        testSuccessResponse(r, Response.Status.CREATED, 1);
        
        r = (JongoSuccess)controller.getResource("user", "name", newMock.name, limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        JongoError err = (JongoError)controller.insertResource("user", "id", "");
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.insertResource("user", "id", new HashMap<String,String>());
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        newMock = UserMock.getRandomInstance();
        Map<String, String> wrongUserParams = newMock.toMap();
        wrongUserParams.put("birthday", "0000"); // in wrong format
        err = (JongoError)controller.insertResource("user", "id", wrongUserParams);
        testErrorResponse(err, Response.Status.BAD_REQUEST, "42561", new Integer(-5561));
        
        wrongUserParams = newMock.toMap();
        wrongUserParams.put("age", null); // age can be null
        r = (JongoSuccess)controller.insertResource("user", "id", wrongUserParams);
        testSuccessResponse(r, Response.Status.CREATED, 1);
        
        wrongUserParams = newMock.toMap();
        wrongUserParams.put("age", ""); // age can't be empty
        err = (JongoError)controller.insertResource("user", "id", wrongUserParams);
        testErrorResponse(err, Response.Status.BAD_REQUEST, "22018", new Integer(-3438));
        
        wrongUserParams = newMock.toMap();
        wrongUserParams.put("name", null); // name can't be null
        err = (JongoError)controller.insertResource("user", "id", wrongUserParams);
        testErrorResponse(err, Response.Status.BAD_REQUEST, "23502", new Integer(-10));
        
        wrongUserParams = newMock.toMap();
        wrongUserParams.put("name", ""); // name can be empty
        r = (JongoSuccess)controller.insertResource("user", "id", wrongUserParams);
        testSuccessResponse(r, Response.Status.CREATED, 1);
        r = (JongoSuccess)controller.getResource("user", "name", newMock.name, limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
    }
    
    @Test
    public void testUpdateResource(){
        JongoSuccess r = (JongoSuccess)controller.updateResource("user", "id", "0", "{\"age\":\"90\"}");
        testSuccessResponse(r, Response.Status.OK, 1);
        
        JongoError err = (JongoError)controller.updateResource("user", "id", "0", "{\"age\":\"\"}"); // age can't be empty
        testErrorResponse(err, Response.Status.BAD_REQUEST, "22018", new Integer(-3438));
        
        err = (JongoError)controller.updateResource("user", "id", "0", "{\"age\":}"); // invalid json
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.updateResource("user", "id", "0", "{\"age\":\"90\", \"birthday\":00X0}"); // invalid date
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.updateResource("user", "id", "0", "{\"age\":\"90\", \"birthday\":\"00X0\"}"); // invalid date
        testErrorResponse(err, Response.Status.BAD_REQUEST, "22007", new Integer(-3407));
        
        r = (JongoSuccess)controller.updateResource("car", "cid", "0", "{\"model\":\"Test$%&·$&%·$/()=?¿Model\"}"); //custom id
        testSuccessResponse(r, Response.Status.OK, 1);
    }
    
    @Test
    public void testRemoveResource(){
        UserMock newMock = UserMock.getRandomInstance();
        JongoSuccess r = (JongoSuccess)controller.insertResource("user", "id", newMock.toJSON());
        testSuccessResponse(r, Response.Status.CREATED, 1);
        
        r = (JongoSuccess)controller.getResource("user", "name", newMock.name, limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        String id = getId(r, "id");
        Assert.assertNotNull(id);
        
        r = (JongoSuccess)controller.deleteResource("user", "id", id);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        JongoError err = (JongoError)controller.deleteResource("user", "id", "");
        testErrorResponse(err, Response.Status.BAD_REQUEST, "22018", new Integer(-3438));
    }
    
    @Test
    public void testFindByColumn(){
        JongoSuccess r = (JongoSuccess)controller.findByColumn("comments","car_id","0", limit, order);
        testSuccessResponse(r, Response.Status.OK, 2);
        
        r = (JongoSuccess)controller.findByColumn("comments","car_id","2", limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        JongoError err = (JongoError)controller.findByColumn("comments","car_id","1", limit, order);
        testErrorResponse(err, Response.Status.NOT_FOUND, null, null);
        
        err = (JongoError)controller.findByColumn("comments","car_id_grrr","2", limit, order);
        testErrorResponse(err, Response.Status.BAD_REQUEST, "42501", new Integer(-5501));
        
        err = (JongoError)controller.findByColumn("comments","car_id","", limit, order);
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.findByColumn("comments","","0", limit, order);
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.findByColumn("","id","0", limit, order);
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
    }
    
    private void testErrorResponse(JongoError err, Response.Status expectedStatus, String expectedSqlState, Integer expectedSqlCode){
        Assert.assertEquals(expectedStatus, err.getStatus());
        Assert.assertNotNull(err.getMessage());
        Assert.assertFalse(err.isSuccess());
        Assert.assertEquals(expectedSqlState, err.getSqlState());
        Assert.assertEquals(expectedSqlCode, err.getSqlCode());
        l.debug(err.getMessage());
    }
    
    private void testSuccessResponse(JongoSuccess r, Response.Status expectedStatus, int expectedResults){
        List<Row> rows = r.getRows();
        Assert.assertEquals(expectedStatus, r.getStatus());
        Assert.assertTrue(r.isSuccess());
        Assert.assertEquals(expectedResults, rows.size());
    }
    
    private void testDynamicFinder(String resource, String finder, int expectedResults, String...  arr){
        JongoSuccess r = (JongoSuccess)controller.findByDynamicFinder(resource,finder, Arrays.asList(arr), limit, order);
        testSuccessResponse(r, Response.Status.OK, expectedResults);
    }
    
    private String getId(final JongoSuccess response, final String id){
        for(Row row : response.getRows()){
            for(String k : row.getCells().keySet()){
                if(k.equalsIgnoreCase(id)){
                    return row.getCells().get(k);
                }
            }
        }
        return null;
    }
}
