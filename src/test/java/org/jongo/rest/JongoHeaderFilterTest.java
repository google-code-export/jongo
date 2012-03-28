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
package org.jongo.rest;

import com.sun.jersey.spi.container.ContainerResponse;
import javax.ws.rs.core.Response;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoHeaderFilterTest {
    
    JongoHeaderFilter f = new JongoHeaderFilter();
    
    @Test
    public void testFilter() {
        ContainerResponse cr = new ContainerResponse(null, null, null);
        cr.setResponse(Response.ok("xxxxxxxxxxxxxxxxx").build());
        cr = f.filter(null, cr);
        assertEquals("PvgoOWefBe8mDjrJgt6TzQ==", cr.getHttpHeaders().getFirst("Content-MD5"));
        assertEquals(17, cr.getHttpHeaders().getFirst("Content-Length"));
    }
}
