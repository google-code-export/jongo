package org.jongo;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.jongo.jdbc.DynamicFinder;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class DynamicFinderTest extends TestCase {

    public DynamicFinderTest(String testName) {
        super(testName);
    }

    public void test_findByName() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ?";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameAndAgeGreaterThanEquals() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age >= ?";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameEqualsAndAgeIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    public void test_findByAgeEqualsAndNameIsNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE age = ? AND name IS NULL";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameAndAgeEquals() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age = ?";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameLessThanAndAgeGreaterThanEquals() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name < ? AND age >= ?";
        doTest(dynamicQuery, query);
    }

    public void test_findAllByAgeBetweenAndNameEquals() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE age BETWEEN ? AND ? AND name = ?";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameAndAgeIsNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age IS NULL";
        doTest(dynamicQuery, query);
    }

    public void test_findAllByAgeBetween() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE age BETWEEN ? AND ?";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameAndAgeIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameEquals() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ?";
        doTest(dynamicQuery, query);
    }

    public void test_findAllByNameAndAge() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age = ?";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameIsNotNullAndAgeIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name IS NOT NULL AND age IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameIsNullAndAgeIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name IS NULL AND age IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameAndAgeGreaterThan() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age > ?";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameIsNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name IS NULL";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameGreaterThanAndAgeIsNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name > ? AND age IS NULL";
        doTest(dynamicQuery, query);
    }

    public void test_findByNameGreaterThanEqualsAndAgeIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name >= ? AND age IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    public void test_findByAgeAndNameIsNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE age = ? AND name IS NULL";
        doTest(dynamicQuery, query);
    }

    private void doTest(String dynamicQuery, String query) {
        DynamicFinder d = DynamicFinder.valueOf("sometable", dynamicQuery);
        assertTrue(d.getSql().equalsIgnoreCase(query));
    }
    
    private void XtestGenerateDynamicFindersTests(){
        Map<String, String> tests = new HashMap<String, String>();
        tests.put("findByName", "SELECT * FROM sometable WHERE name = ?");
        tests.put("findByNameEquals", "SELECT * FROM sometable WHERE name = ?");
        tests.put("findByNameIsNull", "SELECT * FROM sometable WHERE name IS NULL");
        tests.put("findByNameIsNotNull", "SELECT * FROM sometable WHERE name IS NOT NULL");
        tests.put("findAllByNameAndAge", "SELECT * FROM sometable WHERE name = ? AND age = ?");
        tests.put("findByNameAndAgeEquals", "SELECT * FROM sometable WHERE name = ? AND age = ?");
        tests.put("findByNameAndAgeGreaterThan", "SELECT * FROM sometable WHERE name = ? AND age > ?");
        tests.put("findByNameAndAgeGreaterThanEquals", "SELECT * FROM sometable WHERE name = ? AND age >= ?");
        tests.put("findByNameLessThanAndAgeGreaterThanEquals", "SELECT * FROM sometable WHERE name < ? AND age >= ?");
        tests.put("findByNameAndAgeIsNull", "SELECT * FROM sometable WHERE name = ? AND age IS NULL");
        tests.put("findByNameAndAgeIsNotNull", "SELECT * FROM sometable WHERE name = ? AND age IS NOT NULL");
        tests.put("findByNameEqualsAndAgeIsNotNull", "SELECT * FROM sometable WHERE name = ? AND age IS NOT NULL");
        tests.put("findByNameIsNullAndAgeIsNotNull", "SELECT * FROM sometable WHERE name IS NULL AND age IS NOT NULL");
        tests.put("findByAgeAndNameIsNull", "SELECT * FROM sometable WHERE age = ? AND name IS NULL");
        tests.put("findByAgeEqualsAndNameIsNull", "SELECT * FROM sometable WHERE age = ? AND name IS NULL");
        tests.put("findByNameIsNotNullAndAgeIsNotNull", "SELECT * FROM sometable WHERE name IS NOT NULL AND age IS NOT NULL");
        tests.put("findByNameGreaterThanAndAgeIsNull", "SELECT * FROM sometable WHERE name > ? AND age IS NULL");
        tests.put("findByNameGreaterThanEqualsAndAgeIsNotNull", "SELECT * FROM sometable WHERE name >= ? AND age IS NOT NULL");
        tests.put("findAllByAgeBetween", "SELECT * FROM sometable WHERE age BETWEEN ? AND ?");
        tests.put("findAllByAgeBetweenAndNameEquals", "SELECT * FROM sometable WHERE age BETWEEN ? AND ? AND name = ?");

        for(String str : tests.keySet()){
            String result = tests.get(str);
            StringBuilder b = new StringBuilder("public void test_");
            b.append(str);
            b.append("(){\nString dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split(\"_\")[1];\nString query = \"");
            b.append(result);
            b.append("\";\n");
            b.append("doTest(dynamicQuery, query);\n}");
            System.out.println(b.toString());
        }
        
    }
}