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

package org.jongo.demo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.dbutils.QueryRunner;
import org.jongo.config.AbstractDatabaseConfiguration;
import org.jongo.config.DatabaseConfiguration;
import org.jongo.domain.JongoQuery;
import org.jongo.domain.JongoTable;
import org.jongo.enums.JDBCDriver;
import org.jongo.jdbc.JDBCConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create the database tables for the demo
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Demo {
    
    private static final Logger l = LoggerFactory.getLogger(Demo.class);
    
    public static void generateDemoDatabases(final Set<String> databases){
        for( String k : databases)
            generateDemoDatabase(k);
    }
    
    private static void generateDemoDatabase(final String database){
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource(database));
        QueryRunner adminRun = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        try {
            l.info("Generating Demo Tables in database " + database);
            run.update(getCreateUserTable());
            run.update(getCreateMakersTable());
            run.update(getCreateCarsTable());
            run.update(getCreateCommentsTable());
            run.update(getCreatePicturesTable());
            run.update(getCreateSalesStatsTable());
            run.update(getCreateSalesByMakerAndModelStatsTable());
            
            l.info("Generating Demo Data in database " + database);
            
            final String insertUserQuery = "INSERT INTO user (name, age, birthday, credit) VALUES (?,?,?,?)";
            run.update(insertUserQuery, "foo", 30, "1982-12-13", 32.5);
            run.update(insertUserQuery, "bar", 33, "1992-01-15", 0);
            
            for(CarMaker maker: CarMaker.values()){
                run.update("INSERT INTO maker (name, realname) VALUES (?,?)", maker.name(), maker.getRealName());
            }
            
            final String insertCar = "INSERT INTO car (maker, model, year, fuel, transmission, currentMarketValue, newValue) VALUES (?,?,?,?,?,?,?)";
            run.update(insertCar, "CITROEN", "C2", 2008, "Gasoline", "Manual", 9000, 13000);
            run.update("INSERT INTO car (maker, model, year, transmission, currentMarketValue, newValue) VALUES (?,?,?,?,?,?)", "FIAT", "500", 2010,"Manual", 19000, 23.000);
            run.update(insertCar, "BMW", "X5", 2011, "Diesel", "Automatic", 59000, 77000);
            
            final String insertComment = "INSERT INTO comments (car_id, comment) VALUES (?,?)";
            run.update(insertComment, 0, "The Citroen C2 is a small car with a great attitude"); 
            run.update(insertComment, 0, "I Love my C2");
            run.update(insertComment, 2, "BMW's X5 costs too much for what it's worth. Checkout http://www.youtube.com/watch?v=Bg1TB4dRobY"); 
            
            final String insertPicture = "INSERT INTO pictures (car_id, picture) VALUES (?,?)";
            run.update(insertPicture, 0, "http://www.babez.de/citroen/c2/picth01.jpg"); 
            run.update(insertPicture, 0, "http://www.babez.de/citroen/c2/pic02.jpg"); 
            run.update(insertPicture, 0, "http://www.babez.de/citroen/c2/picth03.jpg"); 
            
            run.update(insertPicture, 1, "http://www.dwsauto.com/wp-content/uploads/2008/07/fiat-500-photo.jpg"); 
            run.update(insertPicture, 1, "http://www.cochesadictos.com/coches/fiat-500/imagenes/index1.jpg"); 
            run.update(insertPicture, 1, "http://www.cochesadictos.com/coches/fiat-500/imagenes/index4.jpg");
            
            run.update(insertPicture, 2, "http://www.coches21.com/fotos/100/bmw_x5_457.jpg"); 
            run.update(insertPicture, 2, "http://www.coches21.com/fotos/100/bmw_x5_460.jpg"); 
            run.update(insertPicture, 2, "http://www.coches21.com/modelos/250/bmw_x5_65.jpg");
            
            // generate some random data for the stats page
            for(int year = 2000; year < 2012; year++){
                for (int month = 1; month <= 12; month++){
                    int val = 500 + (int)(Math.random() * ((2000 - 500) + 1));
                    run.update("INSERT INTO sales_stats (year, month, sales) VALUES (?,?,?)", year, month, val);
                    for(CarMaker maker: CarMaker.values()){
                        val = (int)(Math.random() * ((100 - 0) + 1));
                        run.update("INSERT INTO maker_stats (year, month, sales, maker) VALUES (?,?,?,?)", year, month, val, maker.name());
                    }
                }
            }
            
            l.info("Inserting tables in JongoAdmin");
            adminRun.update(JongoTable.CREATE, database, "user", "id", 3);
            adminRun.update(JongoTable.CREATE, database, "car", "cid", 3);
            adminRun.update(JongoTable.CREATE, database, "comments", "id", 3);
            adminRun.update(JongoTable.CREATE, database, "maker", "id", 1);
            adminRun.update(JongoTable.CREATE, database, "pictures", "id", 3);
            adminRun.update(JongoTable.CREATE, database, "sales_stats", "id", 1);
            adminRun.update(JongoTable.CREATE, database, "maker_stats", "id", 1);
            adminRun.update(JongoQuery.CREATE, "yearSummary", "SELECT year FROM maker_stats GROUP BY year", "Returns all years we have data for");
            
        } catch (SQLException ex) {
            l.error("Failed to create demo tables " + ex.getMessage());
        }
    }
    
    private static String getCreateSalesByMakerAndModelStatsTable(){
        StringBuilder b = new StringBuilder();
        b.append("CREATE TABLE maker_stats (");
        b.append("id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, ");
        b.append("year INTEGER, month INTEGER, maker VARCHAR(50), sales INTEGER)");
        return b.toString();
    }
    
    private static String getCreateSalesStatsTable(){
        StringBuilder b = new StringBuilder();
        b.append("CREATE TABLE sales_stats (");
        b.append("id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, ");
        b.append("year INTEGER, month INTEGER, sales INTEGER)");
        return b.toString();
    }
    
    private static String getCreatePicturesTable(){
        StringBuilder b = new StringBuilder();
        b.append("CREATE TABLE pictures (");
        b.append("id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, ");
        b.append("car_id INTEGER, picture VARCHAR(255))");
        return b.toString();
    }
    
    private static String getCreateCommentsTable(){
        StringBuilder b = new StringBuilder();
        b.append("CREATE TABLE comments (");
        b.append("id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, ");
        b.append("car_id INTEGER, comment VARCHAR(255))");
        return b.toString();
    }
    
    private static String getCreateMakersTable(){
        StringBuilder b = new StringBuilder();
        b.append("CREATE TABLE maker (");
        b.append("id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, ");
        b.append("name VARCHAR(50), realname VARCHAR(50))");
        return b.toString();
    }

    private static String getCreateCarsTable(){
        StringBuilder b = new StringBuilder();
        b.append("CREATE TABLE car (");
        b.append("cid INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, ");
        b.append("maker VARCHAR(50), model VARCHAR(25), year INTEGER, fuel VARCHAR(25), transmission VARCHAR(25), ");
        b.append("created DATE, lastupdate TIMESTAMP, currentMarketValue DECIMAL(10,2), newValue DECIMAL(10,2)) ");
        return b.toString();
    }
    
    private static String getCreateUserTable(){
        StringBuilder b = new StringBuilder();
        b.append("CREATE TABLE user (");
        b.append("id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, ");
        b.append("name VARCHAR(25), age INTEGER, birthday DATE, lastupdate TIMESTAMP, credit DECIMAL(6,2)) ");
        return b.toString();
    }
    
    public static Map<String, DatabaseConfiguration> getDemoDatabasesConfiguration(){
        Map<String, DatabaseConfiguration> demos = new HashMap<String, DatabaseConfiguration>();
        demos.put("demo1", AbstractDatabaseConfiguration.instanceOf("demo1", JDBCDriver.HSQLDB, "demo", "demo", "jdbc:hsqldb:mem:demo1"));
//        demos.put("demo2", AbstractDatabaseConfiguration.instanceOf("demo2", JDBCDriver.HSQLDB, "demo", "demo", "jdbc:hsqldb:mem:demo2"));
        return demos;
    }
}
