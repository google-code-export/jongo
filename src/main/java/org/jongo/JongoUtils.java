package org.jongo;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
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
}
