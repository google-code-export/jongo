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

package org.jongo.jdbc;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jongo.JongoUtils;
import org.jongo.enums.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class DynamicFinder {

    private static final Logger l = LoggerFactory.getLogger(DynamicFinder.class);
    public static final String FINDBY = "findBy";
    public static final String FINDALLBY = "findAllBy";
    private String table;
    private String command;
    private String firstColumn;
    private Operator firstOperator;
    private Operator booleanOperator;
    private String secondColumn;
    private Operator secondOperator;
    private final String sql;

    public static DynamicFinder valueOf(String table, final String query, final String... values) {
        l.debug("Generating dynamic finder for " + query + " with values: [ " + StringUtils.join(values, ",") + "]");
        String str = query;
        String cmd = null;
        if (str.contains(FINDBY)) {
            str = str.substring(FINDBY.length());
            cmd = FINDBY;
        } else if (str.contains(FINDALLBY)) {
            str = str.substring(FINDALLBY.length());
            cmd = FINDALLBY;
        } else {
            throw new IllegalArgumentException("Invalid Command " + str);
        }

        String[] strs = JongoUtils.splitCamelCase(str).split("\\ ");
        List<String> columns = new ArrayList<String>();
        List<String> ops = new ArrayList<String>();
        for (String word : strs) {
            if (!Operator.keywords().contains(word)) {
                columns.add(word);
            } else {
                ops.add(word);
            }
        }
        List<Operator> operators = new ArrayList<Operator>();
        String tmp = "";
        for (int i = 0; i < ops.size(); i++) {
            tmp += ops.get(i);
            try {
                Operator op = Operator.valueOf(tmp.toUpperCase());
                if ((op == Operator.GREATERTHAN || op == Operator.LESSTHAN) && ((i + 1) < ops.size())) {
                    Operator ope = Operator.valueOf(ops.get(i + 1).toUpperCase());
                    if (ope == Operator.EQUALS) {
                        op = Operator.valueOf(tmp.toUpperCase() + "EQUALS");
                        ops.remove(i + 1);
                    }
                }
                operators.add(op);
                tmp = "";
            } catch (IllegalArgumentException e) {}
        }
        
        if (operators.isEmpty()) {
            return new DynamicFinder(table, cmd, columns.get(0));
        } else {
            if (columns.size() == 1) {
                return new DynamicFinder(table, cmd, columns.get(0), operators.get(0));
            } else if (columns.size() == 2) {
                if (operators.size() == 1) {
                    return new DynamicFinder(table, cmd, columns.get(0), operators.get(0), columns.get(1));
                } else if (operators.size() == 2) {
                    return new DynamicFinder(table, cmd, columns.get(0), operators.get(0), columns.get(1), operators.get(1));
                } else if (operators.size() == 3) {
                    return new DynamicFinder(table, cmd, columns.get(0), operators.get(0), operators.get(1), columns.get(1), operators.get(2));
                } else {
                    throw new IllegalArgumentException("Too many operators: " + operators.size());
                }

            } else {
                throw new IllegalArgumentException("Too many columns: " + columns.size());
            }
        }
    }
    
    /**
     * Creates a dynamic finder for findByName queries.
     * @param command either findBy or findAllBy
     * @param firstColumn the name of an existing column
     */
    public DynamicFinder(String table, String command, String firstColumn) {
        this.table = table;
        this.command = command;
        this.firstColumn = firstColumn;
        this.firstOperator = Operator.EQUALS;
        this.sql = generateOneColumnQuery(this.table, this.firstColumn, this.firstOperator);
    }

    /**
     * Creates a dynamic finder for findByNameIsNotNull or findByNameIsNull queries.
     * @param command either findBy or findAllBy
     * @param firstColumn  the name of an existing column
     * @param firstOperator only unary operators IsNull or IsNotNull
     */
    public DynamicFinder(String table, String command, String firstColumn, Operator firstOperator) {
        this.table = table;
        this.command = command;
        this.firstColumn = firstColumn;
        this.firstOperator = firstOperator;
        if (this.firstOperator.isUnary()) {
            this.sql = generateNullColumnQuery(this.table, this.firstColumn, this.firstOperator);
        } else if (this.firstOperator == Operator.BETWEEN) {
            this.sql = generateBetweenQuery(this.table, this.firstColumn);
        } else {
            this.sql = generateOneColumnQuery(this.table, this.firstColumn, this.firstOperator);
        }
    }

    /**
     * Creates a dynamic finder for findByNameAndAge
     * @param command command either findBy or findAllBy
     * @param firstColumn  command either findBy or findAllBy
     * @param booleanOperator an operator AND or OR
     * @param secondColumn  command either findBy or findAllBy
     */
    public DynamicFinder(String table, String command, String firstColumn, Operator booleanOperator, String secondColumn) {
        if (!booleanOperator.isBoolean()) {
            throw new IllegalArgumentException("Invalid Operator " + booleanOperator);
        }
        this.table = table;
        this.command = command;
        this.firstColumn = firstColumn;
        this.firstOperator = Operator.EQUALS;
        this.booleanOperator = booleanOperator;
        this.secondColumn = secondColumn;
        this.secondOperator = Operator.EQUALS;
        this.sql = generateTwoColumnQuery(this.table, this.firstColumn, this.firstOperator, this.booleanOperator, this.secondColumn, this.secondOperator);
    }

    /**
     * Creates a dynamic finder for findByNameAndAgeGreaterThan
     * @param command command either findBy or findAllBy
     * @param firstColumn  command either findBy or findAllBy
     * @param booleanOperator an operator AND or OR
     * @param secondColumn the name of an existing column
     * @param secondOperator a binary operator for the second column
     */
    public DynamicFinder(String table, String command, String firstColumn, Operator booleanOperator, String secondColumn, Operator secondOperator) {
        if (!booleanOperator.isBoolean()) {
            throw new IllegalArgumentException("Invalid Operator");
        }
        this.table = table;
        this.command = command;
        this.firstColumn = firstColumn;
        this.firstOperator = Operator.EQUALS;
        this.booleanOperator = booleanOperator;
        this.secondColumn = secondColumn;
        this.secondOperator = secondOperator;
        if (this.firstOperator == Operator.BETWEEN) {
            this.sql = generateBetweenQuery(this.table, this.firstColumn, this.booleanOperator, this.secondColumn, this.secondOperator);
        }else{
            this.sql = generateTwoColumnQuery(this.table, this.firstColumn, this.firstOperator, this.booleanOperator, this.secondColumn, this.secondOperator);
        }
    }

    /**
     * Creates a dynamic finder for findByNameNotEqualsAndAgeGreaterThan
     * @param command command either findBy or findAllBy
     * @param firstColumn  command either findBy or findAllBy
     * @param firstOperator  a binary operator for the second column
     * @param booleanOperator an operator AND or OR
     * @param secondColumn the name of an existing column
     * @param secondOperator a binary operator for the second column
     */
    public DynamicFinder(String table, String command, String firstColumn, Operator firstOperator, Operator booleanOperator, String secondColumn, Operator secondOperator) {
        if (!booleanOperator.isBoolean()) {
            throw new IllegalArgumentException("Invalid Operator");
        }
        this.table = table;
        this.command = command;
        this.firstColumn = firstColumn;
        this.firstOperator = firstOperator;
        this.booleanOperator = booleanOperator;
        this.secondColumn = secondColumn;
        this.secondOperator = secondOperator;
        if (this.firstOperator == Operator.BETWEEN) {
            this.sql = generateBetweenQuery(this.table, this.firstColumn, this.booleanOperator, this.secondColumn, this.secondOperator);
        }else{
            this.sql = generateTwoColumnQuery(this.table, this.firstColumn, this.firstOperator, this.booleanOperator, this.secondColumn, this.secondOperator);
        }
    }

    private static String generateNullColumnQuery(String table, String firstColumn, Operator firstOperator) {
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(table);
        sb.append(" WHERE ");
        sb.append(firstColumn);
        sb.append(" ");
        sb.append(firstOperator.sql());
        return sb.toString();
    }

    private static String generateOneColumnQuery(String table, String firstColumn, Operator firstOperator) {
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(table);
        sb.append(" WHERE ");
        sb.append(firstColumn.toLowerCase());
        sb.append(" ");
        sb.append(firstOperator.sql());
        if (!firstOperator.isUnary()) {
            sb.append(" ?");
        }
        return sb.toString();
    }

    private static String generateTwoColumnQuery(String table, String firstColumn, Operator firstOperator, Operator booleanOperator, String secondColumn, Operator secondOperator) {
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(table);
        sb.append(" WHERE ");
        sb.append(firstColumn);
        sb.append(" ");
        sb.append(firstOperator.sql());
        if (!firstOperator.isUnary()) {
            sb.append(" ? ");
        }else{
            sb.append(" ");
        }
        sb.append(booleanOperator.sql());
        sb.append(" ");
        sb.append(secondColumn);
        sb.append(" ");
        sb.append(secondOperator.sql());
        if (!secondOperator.isUnary()) {
            sb.append(" ?");
        }
        return sb.toString();
    }

    private static String generateBetweenQuery(String table, String firstColumn) {
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(table);
        sb.append(" WHERE ");
        sb.append(firstColumn.toLowerCase());
        sb.append(" BETWEEN ? AND ?");
        return sb.toString();
    }
    
    private static String generateBetweenQuery(String table, String firstColumn, Operator booleanOperator, String secondColumn, Operator secondOperator) {
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(table);
        sb.append(" WHERE ");
        sb.append(firstColumn.toLowerCase());
        sb.append(" BETWEEN ? AND ? ");
        sb.append(booleanOperator.sql());
        sb.append(" ");
        sb.append(secondColumn);
        sb.append(" ");
        sb.append(secondOperator.sql());
        if (!secondOperator.isUnary()) {
            sb.append(" ?");
        }
        return sb.toString();
    }

    public String getCommand() {
        return command;
    }

    public String getSql() {
        return sql;
    }

    public String getTable() {
        return table;
    }

    public boolean findAll() {
        return this.command.equalsIgnoreCase(FINDALLBY);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("DynamicFinder ");
        b.append("{ table : ");
        b.append(table);
        b.append("{ command : ");
        b.append(command);
        b.append("}");
        b.append("{ firstColumn : ");
        b.append(firstColumn);
        b.append(" }");
        b.append("{ firstOperator : ");
        b.append(firstOperator);
        b.append(" }");
        b.append("{ booleanOperator : ");
        b.append(booleanOperator);
        b.append(" }");
        b.append("{ secondColumn : ");
        b.append(secondColumn);
        b.append(" }");
        b.append("{ secondOperator : ");
        b.append(secondOperator);
        b.append(" }");
        return b.toString();
    }
}
