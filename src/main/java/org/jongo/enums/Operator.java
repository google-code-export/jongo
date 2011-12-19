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

package org.jongo.enums;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public enum Operator {
    AND,
    OR,
    BETWEEN,
    LESSTHAN,
    LESSTHANEQUALS,
    GREATERTHAN,
    GREATERTHANEQUALS,
    LIKE,
    ILIKE,
    ISNOTNULL,
    ISNULL,
    NOT,
    EQUALS,
    NOTEQUALS;
    
    private static final List<String> keywords = new ArrayList<String>();
    
    public String sql(){
        switch(this){
            case AND:
                return "AND";
            case OR:
                return "OR";
            case LESSTHAN:
                return "<";
            case LESSTHANEQUALS:
                return "<=";
            case GREATERTHAN:
                return ">";
            case GREATERTHANEQUALS:
                return ">=";
            case LIKE:
            case ILIKE:
                return "LIKE";
            case ISNOTNULL:
                return "IS NOT NULL";
            case ISNULL:
                return "IS NULL";
            case NOT:
                return "NOT";
            case EQUALS:
                return "=";
            case NOTEQUALS:
                return "<>";
            case BETWEEN:
                return "BETWEEN";
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    public boolean isUnary(){
        switch(this){
            case ISNULL:
            case ISNOTNULL:
                return true;
            default:
                return false;
        }
    }
    
    public boolean isBinary(){
        return !this.isUnary() && !this.isBoolean();
    }
    
    public boolean isBoolean(){
        switch(this){
            case AND:
            case OR:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Returns a list with the supported SQL keywords which may render an operator.
     * @return 
     */
    public static List<String> keywords(){
        if(keywords.isEmpty()){
            keywords.add("Is");
            keywords.add("Not");
            keywords.add("Null");
            keywords.add("Greater");
            keywords.add("Less");
            keywords.add("Than");
            keywords.add("Between");
            keywords.add("Equals");
            keywords.add("And");
            keywords.add("Or");
            keywords.add("Like");
        }
        
        return keywords;
    }
}
