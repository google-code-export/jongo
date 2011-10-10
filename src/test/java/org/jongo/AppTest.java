package org.jongo;

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
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Unit test for simple App.
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
    
    public void testCreate(){
        List<List<NameValuePair>> users = getTestValues();
        for(List<NameValuePair> al : users){
            assertTrue(jongoPOSTRequest(jongoUrl + "user", al));
        }
    }

    public void testRetrieve(){
        assertTrue(request(jongoUrl + "user/3", "GET"));
        assertTrue(request(jongoUrl + "user/name/foo", "GET"));
        assertTrue(request(jongoUrl + "user/age/30", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByName&value=foo", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByNameAndAge&values=foo&values=30", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByAge&value=30", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByAgeBetween&values=20&values=40", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByAgeLessThan&value=30", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByNameLike&value='foo'", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByNameIsNotNull", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByNameIsNull", "GET"));
    }
    
    public void testUpdate(){
        
    }
    
    public void testDelete(){
        
    }
    
    public boolean request(final String url, final String method){
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod(method);
            con.setDoOutput(true);
            StringBuilder response = new StringBuilder();
            System.out.println(con.getResponseCode());
            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String strLine = null;
            while((strLine = r.readLine()) != null){
                response.append(strLine);
                response.append("\n");
            }
            
            System.out.println(response);
            if(con.getResponseCode() != Response.Status.OK.getStatusCode()){
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    public boolean jongoPOSTRequest(final String url, final List<NameValuePair> parameters){
        
        final String urlParameters = URLEncodedUtils.format(parameters, "UTF-8");
        
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            con.setDoOutput(true);
            con.setDoInput(true);
            
            DataOutputStream wr = new DataOutputStream (con.getOutputStream());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();
            
            StringBuilder response = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String strLine = null;
            while((strLine = r.readLine()) != null){
                response.append(strLine);
                response.append("\n");
            }
            System.out.println(con.getResponseCode());
            System.out.println(response);
            if(con.getResponseCode() != Response.Status.CREATED.getStatusCode()){
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
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
            
            al.add(new BasicNameValuePair("cols", "name"));
            al.add(new BasicNameValuePair("vals", name));
            al.add(new BasicNameValuePair("cols", "age"));
            al.add(new BasicNameValuePair("vals", age));
            al.add(new BasicNameValuePair("cols", "birthday"));
            al.add(new BasicNameValuePair("vals", birth));
            al.add(new BasicNameValuePair("cols", "lastupdate"));
            al.add(new BasicNameValuePair("vals", birth));
            al.add(new BasicNameValuePair("cols", "credit"));
            al.add(new BasicNameValuePair("vals", "900.15"));
        }
        
        List<NameValuePair> u4 = new ArrayList<NameValuePair>();
        u4.add(new BasicNameValuePair("cols", "name"));
        u4.add(new BasicNameValuePair("vals", "foo"));
        u4.add(new BasicNameValuePair("cols", "age"));
        u4.add(new BasicNameValuePair("vals", "30"));
        u4.add(new BasicNameValuePair("cols", "birthday"));
        u4.add(new BasicNameValuePair("vals", birth));
        u4.add(new BasicNameValuePair("cols", "lastupdate"));
        u4.add(new BasicNameValuePair("vals", birth));
        r.add(u4);
        return r;
        
    }
}
