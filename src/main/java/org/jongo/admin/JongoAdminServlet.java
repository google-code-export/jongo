package org.jongo.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
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
        
        
        
        JongoConfiguration conf = JongoConfiguration.instanceOf();
        
        if(!request.getRemoteAddr().equalsIgnoreCase("0:0:0:0:0:0:0:1") && !request.getRemoteAddr().equalsIgnoreCase(conf.getAdminIp()) ){
            l.debug("Admin console connection from " + request.getRemoteAddr() + " forbidden. Only localhost is allowed");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }else{
            if(request.getPathInfo().equalsIgnoreCase("/jongo.js")){
                readFileAndWriteToResponse(response, request.getPathInfo(), "text/javascript");
            }else if(request.getPathInfo().equalsIgnoreCase("/jongo.css")){
                readFileAndWriteToResponse(response, request.getPathInfo(), "text/css");
            }else{
                readFileAndWriteToResponse(response, "/index.html", MediaType.TEXT_HTML.toString());
            }
        }
    }
    
    private void readFileAndWriteToResponse(HttpServletResponse response, final String filePath, final String media){
        InputStream is = JongoAdminServlet.class.getClass().getResourceAsStream("/org/jongo/admin" + filePath);
        BufferedReader r = null;
        response.setContentType(media);
        
        String str = null;
        try{
            r = new BufferedReader(new InputStreamReader(is));
            while((str = r.readLine()) != null){
                response.getWriter().println(str);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        }catch(IOException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }finally{
            if(r != null){ try { r.close(); } catch(Exception e){}}
            if(is != null){ try { is.close(); } catch(Exception e){}}
        }
    }
}
