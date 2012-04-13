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

import org.jongo.jdbc.DynamicFinder;
import org.jongo.jdbc.LimitParam;
import org.jongo.jdbc.OrderParam;
import org.jongo.sql.Delete;
import org.jongo.sql.Insert;
import org.jongo.sql.Select;
import org.jongo.sql.Update;

/**
 * 
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface Dialect {
    
    public String toStatementString(final Insert insert); // C
    
    public String toStatementString(final Select select); // R
    
    public String toStatementString(final Update update); // U
    
    public String toStatementString(final Delete delete); // D
    
    public String toStatementString(final DynamicFinder finder, final LimitParam limit, final OrderParam order);
    
    public String listOfTablesStatement();
}