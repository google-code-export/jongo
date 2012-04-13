package org.jongo.sql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Update {
    
    private final Table table;
    private String id;
    private Map<String, String> columns = new LinkedHashMap<String, String>();

    public Update(Table table) {
        this.table = table;
    }
    
    public Update addColumn(String columnName, String value) {
        columns.put(columnName, value);
		return this;
	}
    
    public Map<String, String> getColumns() {
        return columns;
    }
    
    public Update setColumns(Map<String, String> columns){
        this.columns = columns;
        return this;
    }

    public String getId() {
        return id;
    }

    public Update setId(String id) {
        this.id = id;
        return this;
    }

    public Table getTable() {
        return table;
    }
    
    public Select getSelect(){
        Select s = new Select(table);
        s.setValue(id);
        return s;
    }
    
    public List<String> getParameters(){
        List<String> params = new ArrayList<String>(this.columns.values());
        params.add(id);
        return params;
    }

    @Override
    public String toString() {
        return "Update{" + "table=" + table + ", id=" + id + ", columns=" + columns + '}';
    }
}
