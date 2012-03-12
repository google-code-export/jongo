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

import com.sun.jersey.server.impl.application.WebApplicationContext;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import junit.framework.Assert;
import org.jongo.config.JongoConfiguration;
import org.jongo.demo.Demo;
import org.jongo.exceptions.StartupException;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.mocks.UserMock;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoSuccess;
import org.jongo.rest.xstream.RowResponse;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class RestControllerTests {
    
    private static final Logger l = LoggerFactory.getLogger(RestControllerTests.class);
    
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
        testSuccessResponse(r, Response.Status.OK, 7);
    }
    
    @Test
    public void testGetResourceMetadata(){
        JongoSuccess r = (JongoSuccess)controller.getResourceMetadata("user");
        testSuccessResponse(r, Response.Status.OK, 6);
        
        JongoError err = (JongoError)controller.getResourceMetadata(null);
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.getResourceMetadata("");
        testErrorResponse(err, Response.Status.BAD_REQUEST, null, null);
        
        err = (JongoError)controller.getResourceMetadata("this table doesnt exists");
        testErrorResponse(err, Response.Status.BAD_REQUEST, "42501", new Integer(-5501));
    }
    
    @Test
    public void testGetResource(){
        
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
        
        // test a table with a custom column
        order.setColumn("cid");
        r = (JongoSuccess)controller.getResource("car", "cid", "1", limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
        
        r = (JongoSuccess)controller.getResource("car", "transmission", "Automatic", limit, order);
        testSuccessResponse(r, Response.Status.OK, 1);
    }
    
    @Test
    public void testInsertResource(){
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
        List<RowResponse> rows = r.getRows();
        Assert.assertEquals(expectedStatus, r.getStatus());
        Assert.assertTrue(r.isSuccess());
        Assert.assertEquals(expectedResults, rows.size());
    }
}
