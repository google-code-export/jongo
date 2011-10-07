package org.jongo.rest;

import java.util.List;
import org.jongo.enums.Operator;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public interface JongoWS {
    
    public String get(final String table, final String id, final String format);
    public String find(final String table, final String format, final String col, final String val, final String op);
    public String find(final String table, final String format, final String col1, final String val1, final String col2, final String val2, final Operator op);
//    public String findAll(final String table, final String... args);
    public String insert(final String table, final String format, final List<String> cols, final List<String> vals);
//    public String update(final String table, final String id, final String... args);
//    public String delete(final String table, final String id);
    
}
