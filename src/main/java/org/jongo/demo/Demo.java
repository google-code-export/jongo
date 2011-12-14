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
import org.apache.commons.dbutils.QueryRunner;
import org.jongo.enums.JDBCDriver;
import org.jongo.jdbc.JDBCConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create the database tables for the demo
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Demo {
    
    public static final JDBCDriver driver = JDBCDriver.HSQLDB;
    public static final String user = "demo";
    public static final String pass = "demo";
    public static final String db = "jdbc:hsqldb:mem:demo";
    
    private static final Logger l = LoggerFactory.getLogger(Demo.class);
    
    private static final String createUserTable = "CREATE TABLE user (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, "
                                                + "name VARCHAR(25), age INTEGER, birthday DATE, lastupdate TIMESTAMP, credit DECIMAL(6,2)) ";
    
    private static final String createCarsTable = "CREATE TABLE car (cid INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, "
                                                + "maker INTEGER, model VARCHAR(25), year INTEGER, fuel VARCHAR(25), transmission VARCHAR(25),"
                                                + "created DATE, lastupdate TIMESTAMP, currentMarketValue DECIMAL(6,2), newValue DECIMAL(6,2)) ";
    
    private static final String createMakersTable = "CREATE TABLE maker (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, name VARCHAR(50), realname VARCHAR(50))";
    
    private static final String createCommentsTable = "CREATE TABLE comments (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, "
                                                + "cid INTEGER, comment VARCHAR(255))";
    
    private static final String createPicturesTable = "CREATE TABLE pictures (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, "
                                                + "cid INTEGER, picture VARCHAR(255))";
    
    private static final String createSalesStatsTable = "CREATE TABLE sales_stats (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, "
                                                 + "year INTEGER, month INTEGER, sales INTEGER)";
    
    private static final String createSalesByMakerAndModelStatsTable = "CREATE TABLE maker_stats (id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0 INCREMENT BY 1) PRIMARY KEY, "
                                                 + "year INTEGER, month INTEGER, maker VARCHAR(50), sales INTEGER)";
    
    public static void generateDemoDatabase(){
        QueryRunner run = new QueryRunner(JDBCConnectionFactory.getDataSource());
        QueryRunner adminRun = new QueryRunner(JDBCConnectionFactory.getAdminDataSource());
        try {
            l.info("Generating Demo Tables");
            run.update(createUserTable);
            run.update(createMakersTable);
            run.update(createCarsTable);
            run.update(createCommentsTable);
            run.update(createPicturesTable);
            run.update(createSalesStatsTable);
            run.update(createSalesByMakerAndModelStatsTable);
            
            l.info("Generating Demo Data");
            
            run.update("INSERT INTO user (name, age, birthday, credit) VALUES (?,?,?,?)", "foo", 30, "1982-12-13", 32.5);
            run.update("INSERT INTO user (name, age, birthday, credit) VALUES (?,?,?,?)", "bar", 33, "1992-01-15", 0);
            
            for(CarMaker maker: CarMaker.values()){
                run.update("INSERT INTO maker (name, realname) VALUES (?,?)", maker.name(), maker.getRealName());
            }
            
            run.update("INSERT INTO car (maker, model, year, fuel, transmission, currentMarketValue, newValue) VALUES (?,?,?,?,?,?,?)", 14, "C2", "2008", "Gasoline", "Manual", 9.000, 13.000);
            run.update("INSERT INTO car (maker, model, year, transmission, currentMarketValue, newValue) VALUES (?,?,?,?,?,?)", 20, "500", "2010", "Manual", 19.000, 23.000);
            run.update("INSERT INTO car (maker, model, year, fuel, transmission, currentMarketValue, newValue) VALUES (?,?,?,?,?,?,?)", 10, "X5", "2011", "Diesel", "Automatic", 59.000, 73.000);
            
            run.update("INSERT INTO comments (cid, comment) VALUES (?,?)", 1, "The Citroen C2 is a small car with a great attitude"); 
            run.update("INSERT INTO comments (cid, comment) VALUES (?,?)", 1, "I Love my C2"); 
            run.update("INSERT INTO comments (cid, comment) VALUES (?,?)", 3, "BMW's X5 costs too much for what it's worth. Checkout http://www.youtube.com/watch?v=Bg1TB4dRobY"); 
            
            run.update("INSERT INTO pictures (cid, picture) VALUES (?,?)", 1, "http://www.babez.de/citroen/c2/picth01.jpg"); 
            run.update("INSERT INTO pictures (cid, picture) VALUES (?,?)", 1, "http://www.babez.de/citroen/c2/pic02.jpg"); 
            run.update("INSERT INTO pictures (cid, picture) VALUES (?,?)", 1, "http://www.babez.de/citroen/c2/picth03.jpg"); 
            
            run.update("INSERT INTO pictures (cid, picture) VALUES (?,?)", 2, "http://www.dwsauto.com/wp-content/uploads/2008/07/fiat-500-photo.jpg"); 
            run.update("INSERT INTO pictures (cid, picture) VALUES (?,?)", 2, "http://www.cochesadictos.com/coches/fiat-500/imagenes/index1.jpg"); 
            run.update("INSERT INTO pictures (cid, picture) VALUES (?,?)", 2, "http://www.cochesadictos.com/coches/fiat-500/imagenes/index4.jpg");
            
            run.update("INSERT INTO pictures (cid, picture) VALUES (?,?)", 3, "http://www.coches21.com/fotos/100/bmw_x5_457.jpg"); 
            run.update("INSERT INTO pictures (cid, picture) VALUES (?,?)", 3, "http://www.coches21.com/fotos/100/bmw_x5_460.jpg"); 
            run.update("INSERT INTO pictures (cid, picture) VALUES (?,?)", 3, "http://www.coches21.com/modelos/250/bmw_x5_65.jpg");
            
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
            adminRun.update("INSERT INTO JongoTable ( name, customId, permits ) VALUES ( ?, ?, ? )", "user", "id", 3);
            adminRun.update("INSERT INTO JongoTable ( name, customId, permits ) VALUES ( ?, ?, ? )", "car", "cid", 3);
            adminRun.update("INSERT INTO JongoTable ( name, customId, permits ) VALUES ( ?, ?, ? )", "comments", "id", 3);
            adminRun.update("INSERT INTO JongoTable ( name, customId, permits ) VALUES ( ?, ?, ? )", "maker", "id", 1);
            adminRun.update("INSERT INTO JongoTable ( name, customId, permits ) VALUES ( ?, ?, ? )", "pictures", "id", 3);
            adminRun.update("INSERT INTO JongoTable ( name, customId, permits ) VALUES ( ?, ?, ? )", "sales_stats", "id", 1);
            adminRun.update("INSERT INTO JongoTable ( name, customId, permits ) VALUES ( ?, ?, ? )", "maker_stats", "id", 1);
            adminRun.update("INSERT INTO JongoQuery ( name, query, description ) VALUES ( ?, ?, ? )", "allDataForCar", "SELECT * FROM car LEFT JOIN maker ON maker.id = car.maker", "Returns all data for a car");
            
        } catch (SQLException ex) {
            l.error("Failed to create demo tables " + ex.getMessage());
        }
    }
    
}
