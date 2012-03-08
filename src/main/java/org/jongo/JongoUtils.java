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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.jongo.config.JongoConfiguration;
import org.jongo.exceptions.JongoBadRequestException;
import org.jongo.exceptions.StartupException;
import org.jongo.rest.xstream.JongoMapConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of commonly used methods and constants.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoUtils {
    
    private static final Logger l = LoggerFactory.getLogger(JongoUtils.class);
    
    /**
     * Check if a string has the ISO date time format. Uses the ISODateTimeFormat.dateTime() from JodaTime
     * and returns a DateTime instance. The correct format is yyyy-MM-ddTHH:mm:ss.SSSZ
     * @param arg the string to check
     * @return a DateTime instance if the string is in the correct ISO format.
     */
    public static DateTime isDateTime(final String arg){
        if(arg == null)  return null;
        DateTimeFormatter f = ISODateTimeFormat.dateTime();
        DateTime ret = null;
        try{
            ret = f.parseDateTime(arg);
        }catch(IllegalArgumentException e){
            l.debug(arg + " is not a valid ISO DateTime");
        }
        return ret;
    }
    
    /**
     * Check if a string has the ISO date format. Uses the ISODateTimeFormat.date() from JodaTime
     * and returns a DateTime instance. The correct format is yyyy-MM-dd or yyyyMMdd
     * @param arg the string to check
     * @return a DateTime instance if the string is in the correct ISO format.
     */
    public static DateTime isDate(final String arg){
        if(arg == null) return null;
        DateTime ret = null;
        DateTimeFormatter df = null;
        if(arg.contains("-")){
            df = ISODateTimeFormat.date();
        }else{
            df = ISODateTimeFormat.basicDate();
        }
        
        try{
            ret = df.parseDateTime(arg);
        }catch(IllegalArgumentException e){
            l.debug(arg + " is not a valid ISO date");
        }
        
        return ret;
    }
    
    /**
     * Check if a string has the ISO time format. Uses the ISODateTimeFormat.time() from JodaTime
     * and returns a DateTime instance. The correct format is HH:mm:ss.SSSZZ or HHmmss.SSSZ
     * @param arg the string to check
     * @return a DateTime instance if the string is in the correct ISO format.
     */
    public static DateTime isTime(final String arg){
        if(arg == null) return null;
        DateTime ret = null;
        DateTimeFormatter df = null;
        if(arg.contains(":")){
            df = ISODateTimeFormat.time();
        }else{
            df = ISODateTimeFormat.basicTime();
        }
        
        try{
            ret = df.parseDateTime(arg);
        }catch(IllegalArgumentException e){
            l.debug(arg + " is not a valid ISO time");
        }
        
        return ret;
    }
    
    /**
     * Convert a String in camelCase to a String separated by white spaces. 
     * I.E. "thisStringInCamelCase" to "this String In Camel Case"
     * @param s String in camelCase
     * @return same string with white spaces.
     */
    public static String splitCamelCase(String s) {
        return s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z0-9_])"), " ");
    }
    
    /**
     * Convert a List of String to a vararg of Objects. The values are deduced from their format
     * and converted to the corresponding java.sql.Types so they are correctly saved in the databases.
     * @param values a List of String with the values to be converted
     * @return a vararg of java.sql Objects
     */
    public static Object[] parseValues(List<String> values){
        List<Object> res = new ArrayList<Object>();
        
        for(String val : values){
            res.add(parseValue(val));
        }
        
        return res.toArray();
    }
    
    /**
     * Infers the java.sql.Types of the given String and returns the JDBC mappable Object corresponding to it.
     * The conversions are like this:
     * String -> String
     * Numeric -> Integer
     * Date or Time -> Date
     * Decimal -> BigDecimal
     * ??? -> TimeStamp
     * @param val a String with the value to be mapped
     * @return a JDBC mappable object instance with the value
     */
    public static Object parseValue(String val){
        Object ret = null;
        if(!StringUtils.isWhitespace(val) && StringUtils.isNumeric(val)){
            try{
                ret = Integer.valueOf(val);
            }catch(Exception e){
                l.debug(e.getMessage());
            }
        }else{
            DateTime date = JongoUtils.isDateTime(val);
            if(date != null){
                l.debug("Got a DateTime " + date.toString(ISODateTimeFormat.dateTime()));
                ret = new java.sql.Timestamp(date.getMillis());
            }else{
                date = JongoUtils.isDate(val);
                if(date != null){
                    l.debug("Got a Date " + date.toString(ISODateTimeFormat.date()));
                    ret = new java.sql.Date(date.getMillis());
                }else{
                    date = JongoUtils.isTime(val);
                    if(date != null){
                        l.debug("Got a Time " + date.toString(ISODateTimeFormat.time()));
                        ret = new java.sql.Time(date.getMillis());
                    }
                }
            }
            
            if(ret == null && val != null){
                l.debug("Not a datetime. Try someting else. ");
                try{
                    ret = new BigDecimal(val);
                }catch(NumberFormatException e){
                    l.debug(e.getMessage());
                    ret = val;
                }
            }
        }
        return ret;
    }
    
    /**
     * Converts a vararg of Object to a String representation of its content
     * @param params a vararg of objects
     * @return a String like "[1,2,3,5,hello,0.1]"
     */
    public static String varargToString(Object... params){
        StringBuilder b = new StringBuilder("[");
        b.append(StringUtils.join(params, ","));
        b.append("]");
        return b.toString();
    }
    
    /**
     * Read the folder "apps" and returns a List with the directories under it
     * @return a List of String with the name of directories under "apps"
     */
    public static List<String> getListOfApps(){
        List<String> apps = new ArrayList<String>();
        
        File appsDir = new File("apps");
        
        if(appsDir.listFiles() == null){
            l.warn("Failed to read the apps folder. Does it exists?");
        }else{
            for(File dir : appsDir.listFiles()){
                if(dir.isDirectory()){
                    apps.add(dir.getName());
                }
            }
        }
        
        return apps;
    }
    
    /**
     * Reads a String in JSON format and returns a MultivaluedMap representation of it.
     * @return a MultivaluedMap with the keys/values as represented by the incoming JSON string.
     * @throws JongoBadRequestException if the JSON string is not readable.
     */
    public static Map<String, String> getParamsFromJSON(final String json) throws JongoBadRequestException{
        // XStream needs the response to be nested inside an object it can understand
        final String formattedJson = "{\"request\":" + json + "}";
        XStream xStream = new XStream(new JettisonMappedXmlDriver());
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.registerConverter(new JongoMapConverter());
        xStream.alias("request", HashMap.class);
        try{
            Map<String, String> ret = (Map<String, String>)xStream.fromXML(formattedJson);
            if(ret.isEmpty()){
                throw new JongoBadRequestException("Invalid number of arguments for request " + json);
            }
            return ret;
        }catch(Exception e){
            throw new JongoBadRequestException(e.getMessage());
        }
    }
    
    public static JongoConfiguration loadConfiguration() throws StartupException{
        JongoConfiguration configuration = null;
        try{
            configuration = JongoConfiguration.instanceOf();
        }catch(IllegalArgumentException e){
            l.error(e.getMessage());
        }
        
        if(configuration == null){
            throw new StartupException("Failed to load configuration", true);
        }
        
        return configuration;
    }
    
    public static String getDatabaseNameFromPath(final UriInfo ui){
        return ui.getBaseUri().getPath().replaceAll("/", "");
    }
}
