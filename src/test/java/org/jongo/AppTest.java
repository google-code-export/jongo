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

import org.junit.Test;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jongo.rest.xstream.JongoError;
import org.jongo.rest.xstream.JongoResponse;
import org.jongo.rest.xstream.JongoSuccess;

import static org.junit.Assert.*;

/**
 * This tests are based on data generated when running in demo mode with the Demo.java class.
 */
public class AppTest {
    
    private static final String jongoUrl = "http://localhost:8080/jongo/";
    private static final SecureRandom random = new SecureRandom();
    
    @Test
    public void testJongo(){
        List<List<NameValuePair>> users = getTestValues();
        for(List<NameValuePair> al : users){
            doTestResponse(jongoPOSTRequest("user?format=xml", al), Response.Status.CREATED, 1);
        }
    }

//    @Test
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
    
    @Test
    public void testUpdate(){
        doTestResponse(jongoJSONRequest("user/2?format=xml", "PUT", "{name:\"foo\", credit:\"30.2\"}"), Response.Status.OK, 1);
        doTestResponse(jongoJSONRequest("user/999?format=xml", "PUT", "{name:\"foo\"}"), Response.Status.NO_CONTENT, 0);
        doTestResponse(jongoJSONRequest("user/1?format=xml", "PUT", "{birthday:\""+ getRandomBirthDate() +"\"}"), Response.Status.OK, 1);
    }
    
//    @Test
    public void Xtest4Delete(){
        assertEquals(request(jongoUrl + "user/3", "DELETE"), 200);
        assertEquals(request(jongoUrl + "user/3", "GET"), 404);
    }
    
    public JongoResponse request(final String url, final String method){
        JongoResponse response = null;
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
    
    public JongoResponse jongoJSONRequest(final String url, final String method, final String jsonParameters){
        JongoResponse response = null;
        
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(jongoUrl + url).openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
            con.setRequestProperty("Content-Length", "" + Integer.toString(jsonParameters.getBytes().length));
            con.setDoOutput(true);
            con.setDoInput(true);
            
            DataOutputStream wr = new DataOutputStream (con.getOutputStream());
            wr.writeBytes (jsonParameters);
            wr.flush ();
            wr.close ();
            
            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder rawresponse = new StringBuilder();
            String strLine = null;
            while((strLine = r.readLine()) != null){
                rawresponse.append(strLine);
                rawresponse.append("\n");
            }
            
            try{
                response = JongoSuccess.fromXML(rawresponse.toString());
            }catch(Exception e){
                response = JongoError.fromXML(rawresponse.toString());
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return response;
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
        DateTimeFormatter f1 = ISODateTimeFormat.dateTime();
        DateTimeFormatter f2 = ISODateTimeFormat.date();
        String birth = f2.parseDateTime(getRandomBirthDate()).toString(f2);
        for(List<NameValuePair> al : r){
            String name = "'" + new BigInteger(100, random).toString(32) + "'";
            String age = String.valueOf(Min + (int)(Math.random() * ((Max - Min) + 1)));
            
            al.add(new BasicNameValuePair("name", name));
            al.add(new BasicNameValuePair("age", age));
            al.add(new BasicNameValuePair("birthday", birth));
            al.add(new BasicNameValuePair("lastupdate", new DateTime().toString(f1)));
            al.add(new BasicNameValuePair("credit", "900.15"));
        }
        
        return r;
        
    }
    
    private String getRandomBirthDate(){
        String year = String.valueOf(1950 + (int)(Math.random() * ((2010 - 1950) + 1)));
        String month = String.valueOf(1 + (int)(Math.random() * ((12 - 1) + 1)));
        String day = String.valueOf(1 + (int)(Math.random() * ((30 - 1) + 1)));
        if(month.length() == 1) month = "0" + month; 
        if(day.length() == 1) day = "0" + day; 
        return year + "-" + month + "-" + day;
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
