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

import org.jongo.enums.Permission;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoTable {
    private int id;
    private String name;
    private String customId;
    private Permission permits;

    public JongoTable(int id, String name, String customId, Permission permits) {
        this.id = id;
        this.name = name;
        this.customId = customId;
        this.permits = permits;
    }

    public JongoTable(String name, String customId, Permission permits) {
        this.name = name;
        this.customId = customId;
        this.permits = permits;
    }
    
    public JongoTable(int id, String name, String customId, int permits) {
        this.id = id;
        this.name = name;
        this.customId = customId;
        this.permits = Permission.valueOf(permits);
    }
    
    public JongoTable(String name, String customId, int permits) {
        this.name = name;
        this.customId = customId;
        this.permits = Permission.valueOf(permits);
    }
    
    public static final String CREATE = "INSERT INTO JongoTable ( name, customId, permits ) VALUES ( ?, ?, ? )";
    public static final String GET = "SELECT * FROM JongoTable WHERE name = ?";
    
    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Permission getPermits() {
        return permits;
    }

    public void setPermits(int permits) {
        this.permits = Permission.valueOf(permits);
    }
    
    public void setPermits(Permission permits) {
        this.permits = permits;
    }
}
