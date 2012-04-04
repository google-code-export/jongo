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
import org.jongo.config.JongoConfiguration;

/**
 * An object to represent the two limit parameters (limit & offset) which are then translated to
 * their correct form in SQL. If no limit parameter is given, the default is loaded from
 * the configuration. The default value for the offset or start is 0.
 * The limit value has a maximum which is set by the configuration. If the given limit value
 * is greater than this maximum, we override the given limit.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class LimitParam {
    
    private static final JongoConfiguration configuration = JongoConfiguration.instanceOf();
    
    private final Integer limit;
    private final Integer start;
    
    public LimitParam(){
        this.limit = configuration.getLimit();
        this.start = 0;
    }
    
    public LimitParam(Integer limit){
        this.limit = getMaxLimit(limit);
        this.start = 0;
    }
    
    public LimitParam(Integer limit, Integer start){
        this.limit = getMaxLimit(limit);
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getStart() {
        return start;
    }
    
    private Integer getMaxLimit(Integer limit){
        if(limit >= configuration.getMaxLimit()){
            limit = configuration.getMaxLimit();
        }
        return limit;
    }
    
    /**
     * From the received parameters, try to obtain a LimitParam object. By default, the LimitParam
     * always has a limit of 25 and an offset (start) in 0.
     * @param pathParams
     * @return 
     */
    public static LimitParam valueOf(final MultivaluedMap<String, String> pathParams){
        Integer l = null;
        if(StringUtils.isNumeric(pathParams.getFirst("limit"))){
            l = Integer.valueOf(pathParams.getFirst("limit"));
        }
        
        Integer o = null;
        if(StringUtils.isNumeric(pathParams.getFirst("offset"))){
            o = Integer.valueOf(pathParams.getFirst("offset"));
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
