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

import org.apache.http.NameValuePair;
import org.jongo.rest.xstream.RowResponse;
import org.jongo.mocks.JongoClient;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.jongo.mocks.UserMock;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoResponse;
import org.jongo.rest.xstream.JongoSuccess;

import static org.junit.Assert.*;

/**
 * This tests are based on data generated when running in demo mode with the Demo.java class.
 */
public class AppOnlineTests {
    
    private static final JongoClient client = new JongoClient();
    
    public static void main(String[] args){
        AppOnlineTests app = new AppOnlineTests();
        app.testJongo();
        app.testErrors();
        app.testDynamicFinders();
        app.testPaging();
        app.testOrdering();
        app.testSQLInject();
    }
    
    public void testJongo(){
        List<UserMock> users = getTestValues();
        List<UserMock> createdusers = new ArrayList<UserMock>();
        for(UserMock user : users){
            doTestResponse(client.doPOST("user?format=xml", user.toNameValuePair()), Response.Status.CREATED, 1);
            createdusers.addAll(doTestResponse(client.doGET("user/name/" + user.name + "?format=xml"), Response.Status.OK, 1));
        }
        
        assertEquals(createdusers.size(), 3);
        
        for(UserMock user : createdusers){
            //generate a new user to update all the values on an existing user
            UserMock newMock = UserMock.getRandomInstance();
            doTestResponse(client.doPUT("user/" + user.id + "?format=xml", newMock.toJSON()), Response.Status.OK, 1);
            UserMock comingUser = doTestResponse(client.doGET("user/" + user.id + "?format=xml"), Response.Status.OK, 1).get(0);
            assertEquals(newMock.name, comingUser.name);
            // now delete them
            doTestResponse(client.doDELETE("user/" + user.id + "?format=xml"), Response.Status.OK, 1);
        }
    }
    
    public void testErrors(){
        doTestResponse(client.doGET("user/999?format=xml"), Response.Status.OK, 0); // this user shouldn't exist. But we don't return an error!
        // let's try an update/insert with invalid data
        doTestResponse(client.doPUT("comments/0?format=xml", "{\"comment\":\"this should fail!\""), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPUT("pictures/0?format=xml", "{}"), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPUT("pictures/0?format=xml", ""), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPOST("pictures?format=xml", "{}"), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPOST("pictures?format=xml", ""), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPOST("pictures?format=xml", new ArrayList<NameValuePair>()), Response.Status.BAD_REQUEST, 0);
        // in the demo, by default, maker is not writtable
        doTestResponse(client.doPOST("maker?format=xml", "{\"maker\":\"this should fail!\",\"id\":1}"), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPUT("maker/0?format=xml", "{\"maker\":\"this should fail!\"}"), Response.Status.BAD_REQUEST, 0);
        // table is not in Jongo
        doTestResponse(client.doPOST("notInJongo?format=xml", "{\"comment\":\"this should fail!\",\"cid\":1}"), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPUT("notInJongo/0?format=xml", "{\"comment\":\"this should fail!\"}"), Response.Status.BAD_REQUEST, 0);
    }
    
    public void testDynamicFinders(){
        doTestResponse(client.doGET("user/dynamic/findAllByAgeBetween?args=18&args=99&format=xml"), Response.Status.OK, 2);
        doTestResponse(client.doGET("user/dynamic/findAllByBirthdayBetween?args=1992-01-01&args=1992-12-31&format=xml"), Response.Status.OK, 1);
        doTestResponse(client.doGET("car/dynamic/findAllByFuelIsNull?format=xml&sort=cid"), Response.Status.OK, 1);
        doTestResponse(client.doGET("car/dynamic/findAllByFuelIsNotNull?format=xml&sort=cid"), Response.Status.OK, 2);
        doTestResponse(client.doGET("user/dynamic/findAllByCreditGreaterThan?args=0&format=xml"), Response.Status.OK, 1);
        doTestResponse(client.doGET("user/dynamic/findAllByCreditGreaterThanEquals?args=0&format=xml"), Response.Status.OK, 2);
        doTestResponse(client.doGET("user/dynamic/findAllByCreditLessThan?args=0&format=xml"), Response.Status.NOT_FOUND, 0);
        doTestResponse(client.doGET("user/dynamic/findAllByCreditLessThanEquals?args=0&format=xml"), Response.Status.OK, 1);
        doTestResponse(client.doGET("sales_stats/dynamic/findAllByLast_updateBetween?args=2000-01-01T00:00:00.000Z&args=2000-06-01T23:55:00.000Z&format=xml"), Response.Status.OK, 6);
    }
    
    public void testPaging(){
        doTestPagingResponse(client.doGET("maker_stats?format=xml"), Response.Status.OK, 25, "id", "0", "24");
        doTestPagingResponse(client.doGET("maker_stats?format=xml&limit=notAllowed"), Response.Status.OK, 25, "id", "0", "24");
        doTestPagingResponse(client.doGET("maker_stats?format=xml&offset=50"), Response.Status.OK, 25, "id", "0", "24");
        doTestPagingResponse(client.doGET("maker_stats?format=xml&limit=50"), Response.Status.OK, 50, "id", "0", "49");
        doTestPagingResponse(client.doGET("maker_stats?format=xml&limit=50&offset=50"), Response.Status.OK, 50, "id", "50", "99");
        doTestResponse(client.doGET("maker_stats?format=xml&limit=50&offset=15550"), Response.Status.OK, 0);
    }
    
    public void testOrdering(){
        doTestPagingResponse(client.doGET("car?format=xml&idField=cid&sort=cid"), Response.Status.OK, 3, "model", "C2", "X5");
        doTestPagingResponse(client.doGET("car?format=xml&sort=year"), Response.Status.OK, 3, "model", "C2", "X5");
        doTestPagingResponse(client.doGET("car?format=xml&sort=year&dir=ASC"), Response.Status.OK, 3, "model", "C2", "X5");
        doTestPagingResponse(client.doGET("car?format=xml&sort=year&dir=DESC"), Response.Status.OK, 3, "model", "X5", "C2");
        doTestPagingResponse(client.doGET("car?format=xml&sort=maker&dir=ASC"), Response.Status.OK, 3, "maker", "BMW", "FIAT");
        doTestPagingResponse(client.doGET("car?format=xml&sort=maker&dir=ASC"), Response.Status.OK, 3, "model", "X5", "500");
        doTestPagingResponse(client.doGET("car?format=xml&sort=maker&dir=DESC"), Response.Status.OK, 3, "maker", "FIAT", "BMW");
        doTestPagingResponse(client.doGET("car?format=xml&sort=maker&dir=DESC"), Response.Status.OK, 3, "model", "500", "X5");
        doTestPagingResponse(client.doGET("car?format=xml&sort=maker&dir=ASC&limit=1"), Response.Status.OK, 1, "model", "X5", "X5");
        doTestPagingResponse(client.doGET("car?format=xml&sort=maker&dir=DESC&limit=2"), Response.Status.OK, 2, "model", "500", "C2");
        doTestPagingResponse(client.doGET("car?format=xml&sort=maker&dir=DESC&limit=2&offset=1"), Response.Status.OK, 2, "model", "C2", "X5");
    }
    
    public void testSQLInject(){
        doTestResponse(client.doPUT("user/0?format=xml", "{\"name\":\"anything' OR 'x'='x'\"}"), Response.Status.OK, 1);
        doTestResponse(client.doGET("user/name/bar%20AND%20age%3D30?format=xml"), Response.Status.NOT_FOUND, 0);
        doTestResponse(client.doGET("user/name/bar%3B%20DROP%20TABLE%20user%3B%20--?format=xml"), Response.Status.NOT_FOUND, 0);
    }
    
    public List<UserMock> getTestValues(){
        List<UserMock> u1 = new ArrayList<UserMock>();
        u1.add(UserMock.getRandomInstance());
        u1.add(UserMock.getRandomInstance());
        u1.add(UserMock.getRandomInstance());
        return u1;
        
    }
    
    private List<UserMock> doTestResponse(JongoResponse r, Response.Status expectedStatus, int expectedCount){
        List<UserMock> users = new ArrayList<UserMock>();
        assertNotNull(r);
        assertEquals(r.getStatus(), expectedStatus);
        if(r instanceof JongoSuccess){
            JongoSuccess s = (JongoSuccess)r;
            List<RowResponse> rows = s.getRows();
            assertTrue(s.isSuccess());
            assertEquals(rows.size(), expectedCount);
            for(RowResponse row : rows){
                users.add(UserMock.instanceOf(row.getColumns()));
            }
        }else{
            JongoError e = (JongoError)r;
            assertFalse(e.isSuccess());
        }
        return users;
    }
    
    private void doTestPagingResponse(JongoResponse r, Response.Status expectedStatus, int expectedCount, String col, String first, String last){
        assertNotNull(r);
        assertEquals(r.getStatus(), expectedStatus);
        if(r instanceof JongoSuccess){
            JongoSuccess s = (JongoSuccess)r;
            List<RowResponse> rows = s.getRows();
            int lastIndex = s.getRows().size() - 1;
            assertTrue(s.isSuccess());
            assertEquals(rows.size(), expectedCount);
            assertEquals(first, rows.get(0).getColumns().get(col));
            assertEquals(last, rows.get(lastIndex).getColumns().get(col));
        }else{
            JongoError e = (JongoError)r;
            assertFalse(e.isSuccess());
        }
    }
}
