package org.jongo.enums;

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
        return !this.isUnary();
    }
    
}
