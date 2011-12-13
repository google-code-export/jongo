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
        List<String> args = new ArrayList<String>();
        for(String key: columns.keySet()){
            String val = columns.get(key);
            if(StringUtils.isNumeric(val)){
                if(StringUtils.isWhitespace(val)){
                    args.add("\"" + key.toLowerCase() + "\"" + ":" + "\"\"");
                }else{
                    args.add("\"" + key.toLowerCase() + "\"" + ":" + val);
                }
            }else{
                args.add("\"" + key.toLowerCase() + "\"" + ":" + "\"" + val + "\"");
            }
        }
        
        StringBuilder b = new StringBuilder();
        b.append("{");
        b.append(StringUtils.join(args, ","));
        b.append("}");
        return b.toString();
    }
}
