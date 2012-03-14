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

/**
 * [{\"name\":\"age\",\"outParameter\":true,\"type\":\"INTEGER\"},{\"value\":\"foo\",\"name\":\"name\",\"outParameter\":false,\"type\":\"VARCHAR\"}]
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class StoredProcedureParam {
    
    private final String value;
    private final String name;
    private final boolean outParameter;
    private final String type;
    
    public StoredProcedureParam(String name, String value, boolean outParameter, String type) {
        this.value = value;
        this.name = name;
        this.outParameter = outParameter;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }

    public boolean isOutParameter() {
        return outParameter;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

//    public static String getCallableStatementCallString(final String queryName, final Integer paramsSize){
//        if(StringUtils.isBlank(queryName))
//            throw new IllegalArgumentException("The name can't be null, empty or blank" );
//        
//        StringBuilder b = new StringBuilder("{CALL ");
//        b.append(queryName);
//        b.append("(");
//        for(int i = 0; i < paramsSize; i++){
//            b.append("?,");
//        }
//        if(b.charAt(b.length() - 1) == ','){
//            b.deleteCharAt(b.length() - 1);
//        }
//        b.append(")}");
//        return b.toString();
//    }
//    
//    private static void addParameters(final CallableStatement cs, final List<StoredProcedureParam> params) throws SQLException, JongoBadRequestException{
//        int i = 1;
//        for(StoredProcedureParam p : params){
//            final Integer sqlType = JongoUtils.sqlTypeOf(p.getType());
//            if(p.isOutParameter()){
//                cs.registerOutParameter(i++, sqlType);
//            }else{
//                switch(sqlType){
//                    case Types.BIGINT:
//                    case Types.INTEGER:
//                    case Types.TINYINT:
//                    case Types.NUMERIC:
//                        cs.setInt(i++, Integer.valueOf(p.getValue())); break;
//                    case Types.DATE:
//                        cs.setDate(i++, (Date)JongoUtils.parseValue(p.getValue())); break;
//                    case Types.TIME:
//                        cs.setTime(i++, (Time)JongoUtils.parseValue(p.getValue())); break;
//                    case Types.TIMESTAMP:
//                        cs.setTimestamp(i++, (Timestamp)JongoUtils.parseValue(p.getValue())); break;
//                    case Types.DECIMAL:
//                        cs.setBigDecimal(i++, (BigDecimal)JongoUtils.parseValue(p.getValue())); break;
//                    case Types.DOUBLE:
//                        cs.setDouble(i++, Double.valueOf(p.getValue())); break;
//                    case Types.FLOAT:
//                        cs.setLong(i++, Long.valueOf(p.getValue())); break;
//                    default:
//                        cs.setString(i++, p.getValue()); break;
//                }
//            }
//        }
//    }
//    public static List<RowResponse> executeQuery(final String database, final String queryName, final List<StoredProcedureParam> params) throws Exception {
//        QueryRunner run = JDBCConnectionFactory.getQueryRunner(database);
//        Connection conn = run.getDataSource().getConnection();
//        final String call = getCallableStatementCallString(queryName, params.size());
//        l.debug("Create the callable statement for " + call);
//        CallableStatement cs = conn.prepareCall(call);
//        addParameters(cs, params);
//        ResultSet rs = cs.executeQuery();
//        JongoResultSetHandler handler = new JongoResultSetHandler(true);
//        List<RowResponse> results = handler.handle(rs);
//        return results;
//    }
//    @Test
//    public void testGetCallableStatementCallString(){
//        String k = JDBCExecutor.getCallableStatementCallString("test", 7);
//        assertEquals("{CALL test(?,?,?,?,?,?,?)}", k);
//        k = JDBCExecutor.getCallableStatementCallString("test", 0);
//        assertEquals("{CALL test()}", k);
//        try{
//            k = JDBCExecutor.getCallableStatementCallString("", 0);
//        }catch(IllegalArgumentException e){
//            assertNotNull(e.getMessage());
//        }
//    }
//    @Test
//    public void testStoredProcedure() throws JongoBadRequestException{
//        JongoSuccess r = (JongoSuccess)controller.executeStoredProcedure("simpleStoredProcedure", "[]");
////        testErrorResponse(err, Response.Status.BAD_REQUEST, "22018", new Integer(-3438));
//        testSuccessResponse(r, Response.Status.OK, 1);
//        
//        String json = "[{\"value\":2,\"name\":\"car_id\",\"outParameter\":false,\"type\":\"INTEGER\"},{\"value\":\"This is a comment\",\"name\":\"comment\",\"outParameter\":false,\"type\":\"VARCHAR\"}]";
//        System.out.println(json);
//        JongoUtils.getStoredProcedureParamsFromJSON(json);
//        r = (JongoSuccess)controller.executeStoredProcedure("insert_comment", json);
//        testSuccessResponse(r, Response.Status.OK, 1);
//    }
//    public JongoResponse executeStoredProcedure(final String query, final String json){
//        l.debug("Executing Stored Procedure " + query);
//        
//        List<StoredProcedureParam> params = null;
//        try {
//            params = JongoUtils.getStoredProcedureParamsFromJSON(json);
//        } catch (JongoBadRequestException ex) {
//            return handleException(ex, query);
//        }
//        
//        JongoResponse response = null;
//        List<RowResponse> results = null;
//        try {
//            results = JDBCExecutor.executeQuery(database, query, params);
//        } catch (Throwable ex){
//            response = handleException(ex, query);
//        }
//        
//        if((results == null || results.isEmpty()) && response == null){
//            response = new JongoError(query, Response.Status.NOT_FOUND, "No results for " + query);
//        }
//        
//        if(response == null){
//            response = new JongoSuccess(query, results);
//        }
//        return response;
//    }
//        public static Integer sqlTypeOf(final String a) throws JongoBadRequestException{
//        final Field [] fields = java.sql.Types.class.getFields();
//        Integer ret = null;
//        for(Field f : fields){
//            if(f.getName().equalsIgnoreCase(a)){
//                try {
//                    ret = f.getInt(java.sql.Types.class);
//                } catch (IllegalArgumentException ex) {
//                    l.error(ex.getMessage());
//                    throw new JongoBadRequestException("Invalid SQL Type: " + a + ". More info at http://docs.oracle.com/javase/6/docs/api/java/sql/Types.html");
//                } catch (IllegalAccessException ex) {
//                    l.error(ex.getMessage()); //this should't happen :)
//                }
//            }
//        }
//        return ret;
//    }
//    
//    public static List<StoredProcedureParam> getStoredProcedureParamsFromJSON(final String json) throws JongoBadRequestException{
//        final String formattedJson = "{\"params\":[{\"param\":" + json + "}]}";
//        XStream xStream = new XStream(new JettisonMappedXmlDriver());
//        xStream.setMode(XStream.NO_REFERENCES);
//        xStream.alias("params", List.class);
//        xStream.alias("param", StoredProcedureParam.class);
//        xStream.alias("type", String.class);
//        try{
//            List<StoredProcedureParam> ret = (ArrayList<StoredProcedureParam>)xStream.fromXML(formattedJson);
//            return ret;
//        }catch(Exception e){
//            throw new JongoBadRequestException(e.getMessage());
//        }
//    }
}
