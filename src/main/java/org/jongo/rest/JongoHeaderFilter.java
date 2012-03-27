/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jongo.rest;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import org.jongo.JongoUtils;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoHeaderFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest cr, ContainerResponse cr1) {
        Object entity = cr1.getResponse().getEntity();
        String md5sum = JongoUtils.getMD5Base64(entity.toString());
        Integer length = JongoUtils.getOctetLength(entity.toString());
        cr1.getHttpHeaders().add("Date", JongoUtils.getDateHeader());
        cr1.getHttpHeaders().add("Content-MD5", md5sum);
        cr1.getHttpHeaders().add("Content-Length", length);
        return cr1;
    }
    
}
