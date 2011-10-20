package org.jongo.rest.xstream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class RowResponse {
    private final int roi;
    private final Map<String, String> columns;// = new HashMap<String, String>();

    public RowResponse(int roi) {
        this.roi = roi;
        this.columns = new HashMap<String, String>();
    }
    
    public RowResponse(int roi, Map<String, String> columns) {
        this.roi = roi;
        this.columns = columns;
    }

    public int getRoi() {
        return roi;
    }

    public Map<String, String> getColumns() {
        return columns;
    }
    
    public String toJSON(){
        StringBuilder b = new StringBuilder();
        b.append("{");
        
        List<String> args = new ArrayList<String>();
        for(String key: columns.keySet()){
            String val = columns.get(key);
            if(StringUtils.isNumeric(val)){
                args.add("\"" + key.toLowerCase() + "\"" + ":" + val);
            }else{
                args.add("\"" + key.toLowerCase() + "\"" + ":" + "\"" + val + "\"");
            }
        }
        b.append(StringUtils.join(args, ","));
        b.append("}");
        return b.toString();
    }
}
