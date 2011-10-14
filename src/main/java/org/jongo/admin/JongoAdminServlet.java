package org.jongo.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InputStreamReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jongo.JongoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoAdminServlet extends HttpServlet{
    
    private static final Logger l = LoggerFactory.getLogger(JongoAdminServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        l.debug("Admin console connection from " + request.getRemoteAddr());
        l.debug(request.getPathInfo());
        
        response.setContentType("text/html");
        
        JongoConfiguration conf = JongoConfiguration.instanceOf();
        
        if(!request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1") && !request.getRemoteAddr().equalsIgnoreCase(conf.getAdminIp()) ){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only localhost is allowed");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }else{
            InputStream is = JongoAdminServlet.class.getClass().getResourceAsStream("/org/jongo/admin/index.html");
            BufferedReader r = new BufferedReader(new InputStreamReader(is));

            response.setStatus(HttpServletResponse.SC_OK);
            String str = null;
            while((str = r.readLine()) != null){
                response.getWriter().println(str);
            }
            r.close();
            is.close();
        }
    }
}
