package org.jongo.domain;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoTable {
    private int id;
    private String name;
    private String customId;
    private int permits;

    public JongoTable(int id, String name, String customId, int permits) {
        this.id = id;
        this.name = name;
        this.customId = customId;
        this.permits = permits;
    }
    
    public JongoTable(String name, String customId, int permits) {
        this.name = name;
        this.customId = customId;
        this.permits = permits;
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

    public int getPermits() {
        return permits;
    }

    public void setPermits(int permits) {
        this.permits = permits;
    }
    
    
}
