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
public class OrderParam {
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";
    
    private final String column;
    private final String direction;
    
    public OrderParam(){
        this.column = "id";
        this.direction = ASC;
    }
    
    public OrderParam(String col){
        if(StringUtils.isBlank(col))
            throw new IllegalArgumentException("Invalid column parameter");
        
        if(ASC.equalsIgnoreCase(col) || DESC.equalsIgnoreCase(col))
            throw new IllegalArgumentException("Invalid column parameter");
        
        this.column = StringUtils.deleteWhitespace(col);
        this.direction = ASC;
    }
    
    public OrderParam(String col, String dir){
        if(StringUtils.isBlank(dir) || StringUtils.isBlank(col))
            throw new IllegalArgumentException("Invalid order parameters");
        
        if(ASC.equalsIgnoreCase(dir)){
            this.direction = ASC;
        }else if(DESC.equalsIgnoreCase(dir)){
            this.direction = DESC;
        }else{
            throw new IllegalArgumentException("Invalid direction parameter");
        }
        this.column = StringUtils.deleteWhitespace(col);
    }

    public String getColumn() {
        return column;
    }

    public String getDirection() {
        return direction;
    }
    
    public static OrderParam valueOf(final MultivaluedMap<String, String> formParams){
        String sort = formParams.getFirst("sort");
        String dir = formParams.getFirst("dir");
        
        OrderParam instance = null;
        if(StringUtils.isBlank(sort)){
            instance = new OrderParam();
        }else{
            if(StringUtils.isBlank(dir)){
                instance = new OrderParam(sort);
            }else{
                instance = new OrderParam(sort, dir);
            }
        }
        
        return instance;
    }
    
    @Override
    public String toString(){
        StringBuilder b = new StringBuilder("{OrderParam:{column:\"");
        b.append(column);
        b.append("\", direction:\"");
        b.append(direction);
        b.append("\"}}");
        return b.toString();
    }
}
