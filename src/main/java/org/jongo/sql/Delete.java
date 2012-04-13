package org.jongo.sql;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Delete {
    
    private final Table table;
    private String id;

    public Delete(Table table) {
        this.table = table;
    }
    
    public String getId() {
        return id;
    }

    public Delete setId(String id) {
        this.id = id;
        return this;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public String toString() {
        return "Delete{" + "table=" + table + ", id=" + id + '}';
    }
}
