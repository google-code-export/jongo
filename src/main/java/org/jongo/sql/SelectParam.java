package org.jongo.sql;

import org.jongo.enums.Operator;

/**
 *
 * @author Alejandro Ayuso
 */
public class SelectParam {
    
    private String columnName;
    private Operator operator;
    private String value;

    public SelectParam() {}
    
    public SelectParam(String columnName, String value) {
        this.columnName = columnName;
        this.operator = Operator.EQUALS;
        this.value = value;
    }

    public SelectParam(String columnName, Operator operator, String value) {
        if(operator.isBoolean())
            throw new IllegalArgumentException("Invalid Operator " + operator.name());
        
        this.columnName = columnName;
        this.operator = operator;
        this.value = value;
    }

    public String getColumnName() {
        return columnName;
    }

    public SelectParam setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public Operator getOperator() {
        return operator;
    }

    public SelectParam setOperator(Operator operator) {
        if(operator.isBoolean())
            throw new IllegalArgumentException("Invalid Operator " + operator.name());
        
        this.operator = operator;
        return this;
    }

    public String getValue() {
        return value;
        
    }

    public SelectParam setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(columnName).append(" ").append(operator.sql()).append(" ?");
        return b.toString();
    }
}
