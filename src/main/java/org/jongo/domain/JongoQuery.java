package org.jongo.domain;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoQuery {
    
    private int id;
    private String name;
    private String query;
    private String description;
    
    public static final String CREATE = "INSERT INTO JongoQuery ( name, query, description ) VALUES ( ?, ?, ? )";

    public JongoQuery(String name, String query, String description) {
        this.name = name;
        this.query = query;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
    
    
    
}
