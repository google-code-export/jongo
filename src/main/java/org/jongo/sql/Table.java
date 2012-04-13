package org.jongo.sql;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Table {
    
    private final String database;
    private final String name;
    private final String primaryKey;
    private final List<String> columns = new ArrayList<String>(); // we could extend this to include the sql.Type

    public Table(String database, String name) {
        if(StringUtils.isBlank(database) || StringUtils.isBlank(name))
            throw new IllegalArgumentException("Argument can't be blank, null or empty");
            
        this.database = database;
        this.name = name;
        this.primaryKey = "id";
    }
    
    public Table(String database, String name, String primaryKey) {
        if(StringUtils.isBlank(database) || StringUtils.isBlank(name))
            throw new IllegalArgumentException("Argument can't be blank, null or empty");
        
        if(StringUtils.isBlank(primaryKey))
            primaryKey = "id";
        
        this.database = database;
        this.name = name;
        this.primaryKey = primaryKey;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getDatabase() {
        return database;
    }

    public String getName() {
        return name;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Table other = (Table) obj;
        if ((this.database == null) ? (other.database != null) : !this.database.equals(other.database)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.primaryKey == null) ? (other.primaryKey != null) : !this.primaryKey.equals(other.primaryKey)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.database != null ? this.database.hashCode() : 0);
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 59 * hash + (this.primaryKey != null ? this.primaryKey.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return this.database + "." + this.name;
    }
}
