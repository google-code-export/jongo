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

import com.thoughtworks.xstream.io.StreamException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoResponse;
import org.jongo.rest.xstream.JongoSuccess;

/**
 * Unit test for simple App. This tests are based on data generated when running in demo mode with the Demo.java object.
 */
public class AppTest extends TestCase {
    
    private static final String jongoUrl = "http://localhost:8080/jongo/";
    private static final SecureRandom random = new SecureRandom();

    public AppTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }
    
    public void test1Create(){
        List<List<NameValuePair>> users = getTestValues();
        for(List<NameValuePair> al : users){
            doTestResponse(jongoPOSTRequest("user?format=xml", al), Response.Status.CREATED, 1);
        }
    }

    public void test2Retrieve(){
        doTestResponse(request("user/1?format=xml", "GET"), Response.Status.OK, 1);
        doTestResponse(request("user/name/foo?format=xml", "GET"), Response.Status.OK, 1);
        doTestResponse(request("user/age/30?format=xml", "GET"), Response.Status.OK, 1);
        
        
//        assertEquals(request(jongoUrl + "user/name/foo", "GET"), 200);
//        assertEquals(request(jongoUrl + "user/age/30", "GET"), 200);
//        assertEquals(request(jongoUrl + "user?query=findByName&value=foo", "GET"), 200);
//        assertEquals(request(jongoUrl + "user?query=findByNameAndAge&values=foo&values=30", "GET"), 200);
//        assertEquals(request(jongoUrl + "user?query=findByAge&value=30", "GET"), 200);
//        assertEquals(request(jongoUrl + "user?query=findByAgeBetween&values=20&values=40", "GET"), 200);
//        assertEquals(request(jongoUrl + "user?query=findByAgeLessThan&value=50", "GET"), 200);
//        assertEquals(request(jongoUrl + "user?query=findByNameLike&value=foo", "GET"), 200);
//        assertEquals(request(jongoUrl + "user?query=findByNameIsNotNull", "GET"), 200);
//        assertEquals(request(jongoUrl + "user?query=findByCreditIsNull", "GET"), 200);
//        assertEquals(request(jongoUrl + "user?query=findByAgeGreaterThanEqualsAndCreditIsNotNull&value=10", "GET"), 200);
    }
    
    public void test3Update(){
        doTestResponse(request("user/3?name=bar", "PUT"), Response.Status.OK, 1);
    }
    
    public void Xtest4Delete(){
        assertEquals(request(jongoUrl + "user/3", "DELETE"), 200);
        assertEquals(request(jongoUrl + "user/3", "GET"), 404);
    }
    
    public JongoSuccess request(final String url, final String method){
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(jongoUrl + url).openConnection();
            con.setRequestMethod(method);
            con.setDoOutput(true);
            BufferedReader r = null;
            
            if(con.getResponseCode() != Response.Status.OK.getStatusCode()){
                r = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }else{
                r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }
            StringBuilder response = new StringBuilder();
            String strLine = null;
            while((strLine = r.readLine()) != null){
                response.append(strLine);
                response.append("\n");
            }
            return JongoSuccess.fromXML(response.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public JongoResponse jongoPOSTRequest(final String url, final List<NameValuePair> parameters){
        
        final String urlParameters = URLEncodedUtils.format(parameters, "UTF-8");
        JongoResponse response = null;
        
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(jongoUrl + url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            con.setDoOutput(true);
            con.setDoInput(true);
            
            DataOutputStream wr = new DataOutputStream (con.getOutputStream());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();
            
            BufferedReader r = null;
            if(con.getResponseCode() != Response.Status.CREATED.getStatusCode()){
                r = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }else{
                r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }
            
            StringBuilder rawresponse = new StringBuilder();
            String strLine = null;
            while((strLine = r.readLine()) != null){
                rawresponse.append(strLine);
                rawresponse.append("\n");
            }
            
            try{
                response = JongoSuccess.fromXML(rawresponse.toString());
            }catch(StreamException e){
                response = JongoError.fromXML(rawresponse.toString());
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return response;
        
    }
    
    
    public List<List<NameValuePair>> getTestValues(){
        List<NameValuePair> u1 = new ArrayList<NameValuePair>();
        List<NameValuePair> u2 = new ArrayList<NameValuePair>();
        List<NameValuePair> u3 = new ArrayList<NameValuePair>();
        List<List<NameValuePair>> r = new ArrayList<List<NameValuePair>>();
        r.add(u1);r.add(u2);r.add(u3);
        
        final int Min = 1;
        final int Max = 100;
        DateTimeFormatter f = ISODateTimeFormat.dateTime();
        String birth = f.print(new DateTime());
        for(List<NameValuePair> al : r){
            String name = "'" + new BigInteger(100, random).toString(32) + "'";
            String age = String.valueOf(Min + (int)(Math.random() * ((Max - Min) + 1)));
            
            al.add(new BasicNameValuePair("name", name));
            al.add(new BasicNameValuePair("age", age));
            al.add(new BasicNameValuePair("birthday", birth));
            al.add(new BasicNameValuePair("lastupdate", birth));
            al.add(new BasicNameValuePair("credit", "900.15"));
        }
        
        List<NameValuePair> u4 = new ArrayList<NameValuePair>();
        u4.add(new BasicNameValuePair("name", "foo"));
        u4.add(new BasicNameValuePair("age", "30"));
        u4.add(new BasicNameValuePair("birthday", birth));
        u4.add(new BasicNameValuePair("lastupdate", birth));
        r.add(u4);
        return r;
        
    }
    
    private void doTestResponse(JongoResponse r, Response.Status expectedStatus, int expectedCount){
        assertNotNull(r);
        assertEquals(r.getStatus(), expectedStatus);
        if(r instanceof JongoSuccess){
            JongoSuccess s = (JongoSuccess)r;
            assertTrue(s.isSuccess());
            assertEquals(s.getRows().size(), expectedCount);
        }else{
            JongoError e = (JongoError)r;
            assertFalse(e.isSuccess());
        }
        
    }
}
