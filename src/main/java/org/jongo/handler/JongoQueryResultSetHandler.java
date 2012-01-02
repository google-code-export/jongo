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
        final String database = rs.getString("database");
        return new JongoQuery(database, name, query, desc);
    }
}
