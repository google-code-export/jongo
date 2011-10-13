package org.jongo.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;
import org.jongo.domain.JongoTable;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoTableResultSetHandler implements ResultSetHandler<JongoTable>{

    @Override
    public JongoTable handle(ResultSet rs) throws SQLException {
        rs.next();
        final int id = rs.getInt("id");
        final String name = rs.getString("name");
        final String customId = rs.getString("customId");
        final int p = rs.getInt("permits");
        return new JongoTable(id, name, customId, p);
    }
    
}
