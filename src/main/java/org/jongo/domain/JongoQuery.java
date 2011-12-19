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

package org.jongo.domain;

/**
 * Represents a JongoQuery object to be used as a model for the administration database.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoQuery {
    
    private int id;
    private String name;
    private String query;
    private String description;
    
    public static final String CREATE = "INSERT INTO JongoQuery ( name, query, description ) VALUES ( ?, ?, ? )";
    public static final String GET = "SELECT * FROM JongoQuery WHERE name = ?";

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
    
    /**
     * Returns a query without break lines which could be inserted by the administration console
     * @return the query from the database without break lines.
     */
    public String getCleanQuery() {
        // the query comes with \n for break lines, so we have to remove them.
        return this.query.replace("\\n", " ");
    }
    
}
