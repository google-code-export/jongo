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

package org.jongo;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jongo.exceptions.JongoBadRequestException;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.jongo.jdbc.DynamicFinder;
import org.junit.Test;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class DynamicFinderTest {

    @Test
    public void test_findByName() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ?";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameAndAgeGreaterThanEquals() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age >= ?";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameEqualsAndAgeIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByAgeEqualsAndNameIsNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE age = ? AND name IS NULL";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameAndAgeEquals() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age = ?";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameLessThanAndAgeGreaterThanEquals() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name < ? AND age >= ?";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findAllByAgeBetweenAndNameEquals() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE age BETWEEN ? AND ? AND name = ?";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameAndAgeIsNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age IS NULL";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findAllByAgeBetween() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE age BETWEEN ? AND ?";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameAndAgeIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameEquals() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ?";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findAllByNameAndAge() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age = ?";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameIsNotNullAndAgeIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name IS NOT NULL AND age IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameIsNullAndAgeIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name IS NULL AND age IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameAndAgeGreaterThan() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name = ? AND age > ?";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameIsNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name IS NULL";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameGreaterThanAndAgeIsNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name > ? AND age IS NULL";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByNameGreaterThanEqualsAndAgeIsNotNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name >= ? AND age IS NOT NULL";
        doTest(dynamicQuery, query);
    }

    @Test
    public void test_findByAgeAndNameIsNull() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE age = ? AND name IS NULL";
        doTest(dynamicQuery, query);
    }
    
    @Test
    public void test_findAllByNameLike() {
        String dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split("_")[1];
        String query = "SELECT * FROM sometable WHERE name LIKE ?";
        doTest(dynamicQuery, query);
    }

    private void doTest(String dynamicQuery, String query) {
        try {
            DynamicFinder d = DynamicFinder.valueOf("sometable", dynamicQuery);
            assertTrue(d.getSql().equalsIgnoreCase(query));
        } catch (JongoBadRequestException ex) {
            System.out.print(ex.getMessage());
        }
    }
    
    private void generateDynamicFindersTests(){
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
            StringBuilder b = new StringBuilder("@Test\npublic void test_");
            b.append(str);
            b.append("(){\nString dynamicQuery = new Exception().getStackTrace()[0].getMethodName().split(\"_\")[1];\nString query = \"");
            b.append(result);
            b.append("\";\n");
            b.append("doTest(dynamicQuery, query);\n}");
            System.out.println(b.toString());
        }
        
    }
}