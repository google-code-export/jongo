package org.jongo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

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
        assertTrue(request(jongoUrl + "user/0", "GET"));
        assertTrue(request(jongoUrl + "user/name/eeo0jc6qmlbum5ttrlc7", "GET"));
        assertTrue(request(jongoUrl + "user/age/5", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByName&value='foo'", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByNameAndAge&values='foo',30", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByAge&value=30", "GET"));
        assertTrue(request(jongoUrl + "user?query=findByAgeBetween&values=30,40", "GET"));
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
        for(List<NameValuePair> al : r){
            String name = "'" + new BigInteger(100, random).toString(32) + "'";
            String age = String.valueOf(Min + (int)(Math.random() * ((Max - Min) + 1)));
            al.add(new BasicNameValuePair("cols", "name,age"));
            al.add(new BasicNameValuePair("vals", StringUtils.join(new String [] {name, age}, ",")));
        }
        
        return r;
        
    }
}
