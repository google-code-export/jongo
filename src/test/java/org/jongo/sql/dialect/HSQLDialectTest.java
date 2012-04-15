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
package org.jongo.sql.dialect;

import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.sql.Select;
import org.junit.Test;

public class HSQLDialectTest extends SQLDialectTest{
    
    public HSQLDialectTest() {
        d = new HSQLDialect();
    }

    @Test
    @Override
    public void testDelete() {
        // TODO Implement
    }

    @Test
    @Override
    public void testInsert() {
        // TODO Implement
    }

    @Test
    @Override
    public void testSelect() {
        doTest("SELECT * FROM a_table", new Select(table));
        
        doTest("SELECT * FROM a_table WHERE tableId=?", new Select(table).setValue("1"));
        
        doTest("SELECT * FROM a_table WHERE name=?", new Select(table).setValue("1").setColumn("name"));
        
        doTest("SELECT * FROM a_table WHERE name=? LIMIT 25 OFFSET 0",
                new Select(table).setValue("1").setColumn("name").setLimitParam(new LimitParam()));
        
        doTest("SELECT * FROM a_table WHERE tableId=? ORDER BY tableId ASC LIMIT 25 OFFSET 0",
                new Select(table).setValue("1").setLimitParam(l).setOrderParam(new OrderParam(table)));
    }

    @Test
    @Override
    public void testUpdate() {
        // TODO Implement
    }
}
