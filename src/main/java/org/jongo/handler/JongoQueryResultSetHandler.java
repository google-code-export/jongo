package org.jongo.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;
import org.jongo.domain.JongoQuery;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class JongoQueryResultSetHandler  implements ResultSetHandler<JongoQuery>{
    @Override
    public JongoQuery handle(ResultSet rs) throws SQLException {
        rs.next();
        final int id = rs.getInt("id");
        final String name = rs.getString("name");
        final String query = rs.getString("query");
        final String desc = rs.getString("description");
        return new JongoQuery(name, query, desc);
    }
}
