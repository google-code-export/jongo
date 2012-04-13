package org.jongo.sql;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;

public class Select {
    
    private final Table table;
    private String column;
    private String value;
    private OrderParam orderParam;
    private LimitParam limitParam;
    private List<String> columns = new ArrayList<String>();

    public Select(Table table) {
        this.table = table;
    }
    
    public Select setColumn(String column) {
		this.column = column;
		return this;
	}
    
    public Select setValue(String value){
        this.value = value;
        return this;
    }
    
    public Select setOrderParam(OrderParam param){
        this.orderParam = param;
        return this;
    }
    
    public Select setLimitParam(LimitParam param){
        this.limitParam = param;
        return this;
    }

    public Select setColumns(List<String> columns) {
        for(String col : columns)
            addColumn(col);
        return this;
    }

    public Select addColumn(String column){
        this.columns.add(column);
        return this;
    }

    
    public List<String> getColumns() {
        return columns;
    }

    public LimitParam getLimitParam() {
        return limitParam;
    }

    public OrderParam getOrderParam() {
        return orderParam;
    }

    public Table getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }
    
    public String getValue() {
        return value;
    }
    
    public boolean isAllRecords(){
        return StringUtils.isEmpty(value);
    }
    
    public boolean isAllColumns(){
        return columns.isEmpty();
    }

    @Override
    public String toString() {
        return "Select{" + "table=" + table + ", column=" + column + ", value=" + value + ", columns=" + columns + '}';
    }
}
