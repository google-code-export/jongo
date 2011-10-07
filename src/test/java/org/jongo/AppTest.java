package org.jongo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    public AppTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testApp(){
        String result = request("http://localhost:8080/jongo/user?cols=name,age&vals='Alejandro%20Ayuso',30", "PUT");
        assertNotNull(result);
        result = request("http://localhost:8080/jongo/get/user/0", "GET");
        assertNotNull(result);
        result = request("http://localhost:8080/jongo/find/user?col=id&val=1", "GET");
        assertNotNull(result);
    }
    
    public void testAddUser(){
        
    }
    
    public String request(final String url, final String method){
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod(method);
            con.setDoOutput(true);
            StringBuilder response = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String strLine = null;
            while((strLine = r.readLine()) != null){
                response.append(strLine);
                response.append("\n");
            }
            return response.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
