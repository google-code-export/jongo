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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jongo.rest.xstream.JongoMapConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoUtils {
    
    private static final Logger l = LoggerFactory.getLogger(JongoUtils.class);
    
    public static DateTime isDateTime(final String arg){
        DateTimeFormatter f = ISODateTimeFormat.dateTime();
        try{
            return f.parseDateTime(arg);
        }catch(IllegalArgumentException e){
            l.debug(arg + " is not a valid date");
            return null;
        }
    }
    
    public static String splitCamelCase(String s) {
        return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
    }
    
    public static Object[] parseValues(List<String> values){
        List<Object> res = new ArrayList<Object>();
        
        for(String val : values){
            res.add(parseValue(val));
        }
        
        return res.toArray();
    }
    
    public static Object parseValue(String val){
        if(StringUtils.isNumeric(val)){
            try{
                return Integer.valueOf(val);
            }catch(Exception e){
                l.debug(e.getMessage());
            }
        }else{
            DateTime date = JongoUtils.isDateTime(val);
            if(date != null){
                return new java.sql.Date(date.getMillisOfDay());
            }else{
                try{
                    return new BigDecimal(val);
                }catch(NumberFormatException e){
                    l.debug(e.getMessage());
                    return val;
                }
            }
        }
        return val;
    }
    
    public static String varargToString(Object... params){
        StringBuilder b = new StringBuilder("[");
        b.append(StringUtils.join(params, ","));
        b.append("]");
        return b.toString();
    }
    
    public static List<String> getListOfApps(){
        List<String> apps = new ArrayList<String>();
        
        File appsDir = new File("apps");
        
        for(File dir : appsDir.listFiles()){
            if(dir.isDirectory()){
                apps.add(dir.getName());
            }
        }
        
        return apps;
    }
    
    public static MultivaluedMap<String, String> getParamsFromJSON(final String json){
        // XStream needs the response to be nested inside an object it can understand
        final String formattedJson = "{\"request\":" + json + "}";
        XStream xStream = new XStream(new JettisonMappedXmlDriver());
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.registerConverter(new JongoMapConverter());
        xStream.alias("request", MultivaluedMap.class);
        return (MultivaluedMap<String, String>)xStream.fromXML(formattedJson);
    }
}
