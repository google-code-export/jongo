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

import org.jongo.config.JongoConfiguration;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import org.jongo.exceptions.StartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Jongo {

    private static final Logger l = LoggerFactory.getLogger(Jongo.class);
    
    public static void main(String[] args) throws StartupException{
        l.info("Starting Jongo in Test Mode");
        
        JongoConfiguration configuration = JongoUtils.loadConfiguration();
        
        StringBuilder url = new StringBuilder("http://");
        url.append(configuration.getIp());
        url.append(":");
        url.append(configuration.getPort());
        url.append("/");
        
        
        l.info("Starting Jongo in " + url);
        try {
            HttpServer server = HttpServerFactory.create(url.toString());
            server.start();
        } catch (IOException ex) {
            l.error("Failed to open socket. Quitting");
            l.error(ex.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException ex) {
            l.error("Invalid URL. Fix your configuration. Quitting");
            l.error(ex.getMessage());
            System.exit(1);
        }
    }
}
