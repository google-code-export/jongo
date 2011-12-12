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
import org.junit.Test;
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
public class AppTest {
    
    private static final JongoClient client = new JongoClient();
    
    @Test
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
    
    @Test
    public void testErrors(){
        doTestResponse(client.doGET("user/999?format=xml"), Response.Status.OK, 0); // this user shouldn't exist. But we don't return an error!
        // let's try an update7insert with invalid data
        doTestResponse(client.doPUT("comments/0?format=xml", "{\"comment\":\"this should fail!\""), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPUT("pictures/0?format=xml", "{}"), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPUT("pictures/0?format=xml", ""), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPOST("pictures?format=xml", "{}"), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPOST("pictures?format=xml", ""), Response.Status.BAD_REQUEST, 0);
        doTestResponse(client.doPOST("pictures?format=xml", new ArrayList<NameValuePair>()), Response.Status.BAD_REQUEST, 0);
        // in the demo, by default, comments is not writtable
        doTestResponse(client.doPOST("comments?format=xml", "{\"comment\":\"this should fail!\",\"cid\":1}"), Response.Status.FORBIDDEN, 0);
        doTestResponse(client.doPUT("comments/0?format=xml", "{\"comment\":\"this should fail!\"}"), Response.Status.FORBIDDEN, 0);
        // table is not in Jongo
        doTestResponse(client.doPOST("notInJongo?format=xml", "{\"comment\":\"this should fail!\",\"cid\":1}"), Response.Status.NOT_FOUND, 0);
        doTestResponse(client.doPUT("notInJongo/0?format=xml", "{\"comment\":\"this should fail!\"}"), Response.Status.NOT_FOUND, 0);
    }
    
    @Test
    public void testDynamicFinders(){
        doTestResponse(client.doGET("user/dynamic/findAllByAgeBetween?values=18&values=99&format=xml"), Response.Status.OK, 2);
        doTestResponse(client.doGET("user/dynamic/findAllByBirthdayBetween?values=1992-01-01&values=1992-12-31&format=xml"), Response.Status.OK, 1);
        doTestResponse(client.doGET("car/dynamic/findAllByFuelIsNull?format=xml"), Response.Status.OK, 1);
        doTestResponse(client.doGET("car/dynamic/findAllByFuelIsNotNull?format=xml"), Response.Status.OK, 2);
        doTestResponse(client.doGET("user/dynamic/findAllByCreditGreaterThan?value=0&format=xml"), Response.Status.OK, 1);
        doTestResponse(client.doGET("user/dynamic/findAllByCreditGreaterThanEquals?value=0&format=xml"), Response.Status.OK, 2);
        doTestResponse(client.doGET("user/dynamic/findAllByCreditLessThan?value=0&format=xml"), Response.Status.NOT_FOUND, 0);
        doTestResponse(client.doGET("user/dynamic/findAllByCreditLessThanEquals?value=0&format=xml"), Response.Status.OK, 1);
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
}
