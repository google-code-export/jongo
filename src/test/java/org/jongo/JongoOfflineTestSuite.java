/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jongo;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.jongo.UtilsTest.class, 
    org.jongo.RestControllerTest.class, 
    org.jongo.JDBCExecutorTest.class, 
    org.jongo.XmlXstreamTest.class, 
    org.jongo.DynamicFinderTest.class})
public class JongoOfflineTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
}
