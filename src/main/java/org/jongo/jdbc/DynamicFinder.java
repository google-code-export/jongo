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
    
    private String command;
    private String firstColumn;
    private Operator firstOperator;
    private Operator booleanOperator;
    private String secondColumn;
    private Operator secondOperator;
    private final String sql;
    
    public static DynamicFinder valueOf(final String query, final String... values){
        final String [] splitted = validateAndSplitQuery(query);
        final String command = parseCommand(splitted[0]);
        
        final List<Operator> operators = new ArrayList<Operator>();
        final List<String> columns = new ArrayList<String>();
        
        for(int i = 1; i < splitted.length ; i++){
            try{
                operators.add(Operator.valueOf(splitted[i]));
            }catch(IllegalArgumentException e){
                columns.add(splitted[i]);
            }
        }
        l.debug(operators.toString());
        l.debug(columns.toString());
        
        if(operators.isEmpty()){
            return new DynamicFinder(command, columns.get(0));
        }else{
            if(columns.size() == 1){
                return new DynamicFinder(command, columns.get(0), operators.get(0));
            }else if(columns.size() == 2){
                if(operators.size() == 1){
                    return new DynamicFinder(command, columns.get(0), operators.get(0), columns.get(1));
                }else if(operators.size() == 2){
                    return new DynamicFinder(command, columns.get(0), operators.get(0), columns.get(1), operators.get(1));
                }else if(operators.size() == 3){
                    return new DynamicFinder(command, columns.get(0), operators.get(0), operators.get(1), columns.get(1), operators.get(2));
                }else{
                    throw new IllegalArgumentException("Too many operators: " + operators.size());
                }
                
            }else{
                throw new IllegalArgumentException("Too many columns: " + columns.size());
            }
        }
        
//        if(values == null){
//            if(operators.isEmpty()){
//                return new DynamicFinder(command, columns.get(0));
//            }else{
//                if(columns.size() == 1){
//                    return new DynamicFinder(command, columns.get(0), operators.get(0));
//                }else if(columns.size() == 2){
//                    return new DynamicFinder(command, columns.get(0), operators.get(0), columns.get(1));
//                }else{
//                    throw new IllegalArgumentException("Too many columns: " + columns.size());
//                }
//            }
//        }else if(values.length == 1){
//            if(operators.isEmpty()){
//                return new DynamicFinder(command, columns.get(0));
//            }else{
//                if(columns.size() == 1){
//                    return new DynamicFinder(command, columns.get(0), operators.get(0));
//                }else if(columns.size() == 2){
//                    return new DynamicFinder(command, columns.get(0), operators.get(0), columns.get(1));
//                }else{
//                    throw new IllegalArgumentException("Too many columns: " + columns.size());
//                }
//            }
//        }else if(values.length == 2){
//            if(operators.isEmpty()){
//                return new DynamicFinder(command, columns.get(0));
//            }else{
//                if(columns.size() == 1){
//                    return new DynamicFinder(command, columns.get(0), operators.get(0));
//                }else if(columns.size() == 2){
//                    return new DynamicFinder(command, columns.get(0), operators.get(0), columns.get(1));
//                }else{
//                    throw new IllegalArgumentException("Too many columns: " + columns.size());
//                }
//            }
//        }else{
//            throw new IllegalArgumentException("Too many values " + values.length);
//        }
        
    }
    
    private static String [] validateAndSplitQuery(final String query){
        if(query == null){
            throw new IllegalArgumentException("Empty query");
        }
        
        l.debug("Splitting " + query);
        String [] splitted = query.toUpperCase().split("\\.");
        if(splitted.length < 2 || splitted.length > 6){
            throw new IllegalArgumentException("Invalid query length " + splitted.length);
        }
        
        return splitted;
    }
    
    private static String parseCommand(final String str){
        if(str.equalsIgnoreCase(FINDBY)){
            return FINDBY;
        }else if(str.equalsIgnoreCase(FINDALLBY)){
            return FINDALLBY;
        }else{
            throw new IllegalArgumentException("Invalid Command " + str);
        }
    }

    /**
     * Creates a dynamic finder for findByName queries.
     * @param command either findBy or findAllBy
     * @param firstColumn the name of an existing column
     */
    public DynamicFinder(String command, String firstColumn) {
        this.command = command;
        this.firstColumn = firstColumn;
        this.firstOperator = Operator.EQUALS;
        this.sql = generateOneColumnQuery(this.firstColumn, this.firstOperator);
    }

    /**
     * Creates a dynamic finder for findByNameIsNotNull or findByNameIsNull queries.
     * @param command either findBy or findAllBy
     * @param firstColumn  the name of an existing column
     * @param firstOperator only unary operators IsNull or IsNotNull
     */
    public DynamicFinder(String command, String firstColumn, Operator firstOperator) {
//        if(!firstOperator.isUnary()) throw new IllegalArgumentException("Invalid Operator");
        this.command = command;
        this.firstColumn = firstColumn;
        this.firstOperator = firstOperator;
        if(this.firstOperator.isUnary()){
            this.sql = generateNullColumnQuery(this.firstColumn, this.firstOperator);
        }else{
            this.sql = generateOneColumnQuery(this.firstColumn, this.firstOperator);
        }
    }

    /**
     * Creates a dynamic finder for findByNameAndAge
     * @param command command either findBy or findAllBy
     * @param firstColumn  command either findBy or findAllBy
     * @param booleanOperator an operator AND or OR
     * @param secondColumn  command either findBy or findAllBy
     */
    public DynamicFinder(String command, String firstColumn, Operator booleanOperator, String secondColumn) {
        if(!booleanOperator.isBoolean()) throw new IllegalArgumentException("Invalid Operator " + booleanOperator);
        this.command = command;
        this.firstColumn = firstColumn;
        this.firstOperator = Operator.EQUALS;
        this.booleanOperator = booleanOperator;
        this.secondColumn = secondColumn;
        this.secondOperator = Operator.EQUALS;
        this.sql = generateTwoColumnQuery(this.firstColumn, this.firstOperator, this.booleanOperator, this.secondColumn, this.secondOperator);
    }
    
    /**
     * Creates a dynamic finder for findByNameAndAgeGreaterThan
     * @param command command either findBy or findAllBy
     * @param firstColumn  command either findBy or findAllBy
     * @param booleanOperator an operator AND or OR
     * @param secondColumn the name of an existing column
     * @param secondOperator a binary operator for the second column
     */
    public DynamicFinder(String command, String firstColumn, Operator booleanOperator, String secondColumn, Operator secondOperator) {
        if(!booleanOperator.isBoolean()) throw new IllegalArgumentException("Invalid Operator");
        this.command = command;
        this.firstColumn = firstColumn;
        this.firstOperator = Operator.EQUALS;
        this.booleanOperator = booleanOperator;
        this.secondColumn = secondColumn;
        this.secondOperator = secondOperator;
        this.sql = generateTwoColumnQuery(this.firstColumn, this.firstOperator, this.booleanOperator, this.secondColumn, this.secondOperator);
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
    public DynamicFinder(String command, String firstColumn, Operator firstOperator, Operator booleanOperator, String secondColumn, Operator secondOperator) {
        if(!booleanOperator.isBoolean()) throw new IllegalArgumentException("Invalid Operator");
        this.command = command;
        this.firstColumn = firstColumn;
        this.firstOperator = firstOperator;
        this.booleanOperator = booleanOperator;
        this.secondColumn = secondColumn;
        this.secondOperator = secondOperator;
        this.sql = generateTwoColumnQuery(this.firstColumn, this.firstOperator, this.booleanOperator, this.secondColumn, this.secondOperator);
    }

    public String getCommand() {
        return command;
    }
    
    public String getSql() {
        return sql;
    }
    
    private static String generateNullColumnQuery(String firstColumn, Operator firstOperator){
        StringBuilder sb = new StringBuilder();
        sb.append("WHERE ");
        sb.append(firstColumn);
        sb.append(" ");
        sb.append(firstOperator.sql());
        return sb.toString();
    }
    
    private static String generateOneColumnQuery(String firstColumn, Operator firstOperator){
        StringBuilder sb = new StringBuilder();
        sb.append("WHERE ");
        sb.append(firstColumn);
        sb.append(" ");
        sb.append(firstOperator.sql());
        sb.append(" ?");
        return sb.toString();
    }
    
    private static String generateTwoColumnQuery(String firstColumn, Operator firstOperator, Operator booleanOperator, String secondColumn, Operator secondOperator){
        StringBuilder sb = new StringBuilder();
        sb.append("WHERE ");
        sb.append(firstColumn);
        sb.append(" ");
        sb.append(firstOperator.sql());
        sb.append(" ?");
        sb.append(booleanOperator.sql());
        sb.append(secondColumn);
        sb.append(" ");
        sb.append(secondOperator.sql());
        sb.append(" ?");
        return sb.toString();
    }
    
    
    @Override
    public String toString(){
        StringBuilder b = new StringBuilder("DynamicFinder ");
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
