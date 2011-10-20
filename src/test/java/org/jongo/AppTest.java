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
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jongo.jdbc.DynamicFinder;

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
    
    public void testDynamicFinders(){
        String [] tests = new String [] {
            "findBy.Name",
            "findBy.Name.Equals",
            "findBy.Name.IsNull",
            "findBy.Name.IsNotNull",
            "findBy.Name.And.Age",
            "findBy.Name.And.Age.Equals",
            "findBy.Name.Equals.And.Age.Equals",
            "findBy.Name.And.Age.GreaterThan",
            "findBy.Name.And.Age.GreaterThanEquals",
            "findBy.Name.LessThan.And.Age.GreaterThanEquals",
            "findBy.Name.LessThanEquals.And.Age.GreaterThanEquals",
            "findBy.Name.And.Age.IsNull",
            "findBy.Name.And.Age.IsNotNull",
            "findBy.Name.GreaterThan.And.Age.IsNull",
            "findBy.Name.GreaterThanEquals.And.Age.IsNotNull",
        };
        
        for(String str : tests){
            DynamicFinder f = DynamicFinder.valueOf("foo", str);
            assertNotNull(f);
//            System.out.println(f.getSql());
        }
    }
    
    public void test1Create(){
        if(request(jongoUrl + "user/0", "GET") != 200){
            List<List<NameValuePair>> users = getTestValues();
            for(List<NameValuePair> al : users){
                assertEquals(jongoPOSTRequest(jongoUrl + "user", al), 201);
            }
        }
    }

    public void test2Retrieve(){
        assertEquals(request(jongoUrl + "user/3", "GET"), 200);
        assertEquals(request(jongoUrl + "user/name/foo", "GET"), 200);
        assertEquals(request(jongoUrl + "user/age/30", "GET"), 200);
        assertEquals(request(jongoUrl + "user?query=findBy.Name&value=foo", "GET"), 200);
        assertEquals(request(jongoUrl + "user?query=findBy.Name.And.Age&values=foo&values=30", "GET"), 200);
        assertEquals(request(jongoUrl + "user?query=findBy.Age&value=30", "GET"), 200);
        assertEquals(request(jongoUrl + "user?query=findBy.Age.Between&values=20&values=40", "GET"), 200);
        assertEquals(request(jongoUrl + "user?query=findBy.Age.LessThan&value=50", "GET"), 200);
        assertEquals(request(jongoUrl + "user?query=findBy.Name.Like&value=foo", "GET"), 200);
        assertEquals(request(jongoUrl + "user?query=findBy.Name.IsNotNull", "GET"), 200);
        assertEquals(request(jongoUrl + "user?query=findBy.Credit.IsNull", "GET"), 200);
        assertEquals(request(jongoUrl + "user?query=findBy.Age.GreaterThanEquals.And.Credit.IsNotNull&value=10", "GET"), 200);
    }
    
    public void test3Update(){
        assertEquals(request(jongoUrl + "user/3?name=bar", "PUT"), 200);
        assertEquals(request(jongoUrl + "user/name/bar", "GET"), 200);
        assertEquals(request(jongoUrl + "user?query=findBy.Name&value=foo", "GET"), 404);
    }
    
    public void test4Delete(){
        assertEquals(request(jongoUrl + "user/3", "DELETE"), 200);
        assertEquals(request(jongoUrl + "user/3", "GET"), 404);
    }
    
    public int request(final String url, final String method){
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
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
            return con.getResponseCode();
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }
    
    public int jongoPOSTRequest(final String url, final List<NameValuePair> parameters){
        
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
            
            BufferedReader r = null;
            if(con.getResponseCode() != Response.Status.CREATED.getStatusCode()){
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
            System.out.println(response.toString());
            return con.getResponseCode();
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
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
}
