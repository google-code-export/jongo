package org.jongo.sql;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Insert {
    
	private final Table table;
	private Map<String, String> columns = new LinkedHashMap<String, String>();

    public Insert(Table table) {
        this.table = table;
    }
    
	public Insert addColumn(String columnName, String value) {
        if(StringUtils.isNotEmpty(value))
            columns.put(columnName, value);
		return this;
	}

    public Map<String, String> getColumns() {
        return columns;
    }

    public Table getTable() {
        return table;
    }

    public Insert setColumns(Map<String, String> columns) {
        this.columns = columns;
        return this;
    }
    
    public List<String> getValues(){
        return new ArrayList<String>(this.columns.values());
    }

    @Override
    public String toString() {
        return "Insert{" + "table=" + table + ", columns=" + columns + '}';
    }
}
