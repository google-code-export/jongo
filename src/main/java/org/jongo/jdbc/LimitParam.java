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
package org.jongo.jdbc;

import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class LimitParam {
    private final Integer limit;
    private final Integer start;
    
    public LimitParam(){
        this.limit = 25;
        this.start = 0;
    }
    
    public LimitParam(Integer limit){
        this.limit = limit;
        this.start = 0;
    }
    
    public LimitParam(Integer limit, Integer start){
        this.limit = limit;
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getStart() {
        return start;
    }
    
    public static LimitParam valueOf(final MultivaluedMap<String, String> formParams){
        Integer l = null;
        if(StringUtils.isNumeric(formParams.getFirst("limit"))){
            l = Integer.valueOf(formParams.getFirst("limit"));
        }
        
        Integer o = null;
        if(StringUtils.isNumeric(formParams.getFirst("offset"))){
            o = Integer.valueOf(formParams.getFirst("offset"));
        }
        
        LimitParam instance = null;
        if(l == null){
            instance = new LimitParam();
        }else{
            if(o == null){
                instance = new LimitParam(l);
            }else{
                instance = new LimitParam(l,o);
            }
        }
        return instance;
    }
    
    @Override
    public String toString(){
        StringBuilder b = new StringBuilder("{LimitParam:{limit:");
        b.append(limit);
        b.append(", start: ");
        b.append(start);
        b.append("}}");
        return b.toString();
    }
}
