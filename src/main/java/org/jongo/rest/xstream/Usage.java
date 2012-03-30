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
package org.jongo.rest.xstream;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

/**
 * A singleton which holds usage data for the current running instance.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Usage {
    
    private DateTime start;
    private BigInteger total = BigInteger.ZERO;
    private BigInteger success = BigInteger.ZERO;
    private BigInteger fail = BigInteger.ZERO;
    
    private BigInteger read = BigInteger.ZERO;
    private BigInteger readAll = BigInteger.ZERO;
    private BigInteger create = BigInteger.ZERO;
    private BigInteger update = BigInteger.ZERO;
    private BigInteger delete = BigInteger.ZERO;
    
    private Long readTime = new Long(0);
    private Long createTime = new Long(0);
    private Long updateTime = new Long(0);
    private Long deleteTime = new Long(0);
    
    private BigInteger dynamic = BigInteger.ZERO;
    private BigInteger query = BigInteger.ZERO;
    
    private Usage() {
        this.start = new DateTime();
    }
    
    public static Usage getInstance() {
        return UsageHolder.INSTANCE;
    }
    
    private static class UsageHolder {
        private static final Usage INSTANCE = new Usage();
    }
    
    public long getUptimeInMillis(){
        return this.start.getMillis();
    }
    
    public String getUptime(){
        Period period = new Period(this.start, new DateTime());
        return PeriodFormat.getDefault().print(period);
    }
    
    private synchronized void addGeneral(final Integer success){
        this.total = this.total.add(BigInteger.ONE);
        if(success == Response.Status.CREATED.getStatusCode() || success == Response.Status.OK.getStatusCode()){
            this.success = this.success.add(BigInteger.ONE);
        }else{
            this.fail = this.fail.add(BigInteger.ONE);
        }
    }
    
    public synchronized void addRead(final Long time, final Integer success){
        this.readTime = time;
        this.read = this.read.add(BigInteger.ONE);
        addGeneral(success);
    }
    
    public synchronized void addReadAll(final Long time, final Integer success){
        this.readTime = time;
        this.read = this.readAll.add(BigInteger.ONE);
        addGeneral(success);
    }
    
    public synchronized void addCreate(final Long time, final Integer success){
        this.createTime = time;
        this.create = this.create.add(BigInteger.ONE);
        addGeneral(success);
    }
    
    public synchronized void addUpdate(final Long time, final Integer success){
        this.updateTime = time;
        this.update = this.update.add(BigInteger.ONE);
        addGeneral(success);
    }
    
    public synchronized void addDelete(final Long time, final Integer success){
        this.deleteTime = time;
        this.delete = this.delete.add(BigInteger.ONE);
        addGeneral(success);
    }
    
    public synchronized void addDynamic(final Long time, final Integer success){
        this.dynamic = this.dynamic.add(BigInteger.ONE);
        addGeneral(success);
    }
    
    public synchronized void addQuery(final Long time, final Integer success){
        this.query = this.query.add(BigInteger.ONE);
        addGeneral(success);
    }

    public BigInteger getCreate() {
        return create;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public BigInteger getDelete() {
        return delete;
    }

    public Long getDeleteTime() {
        return deleteTime;
    }

    public BigInteger getDynamic() {
        return dynamic;
    }

    public BigInteger getFail() {
        return fail;
    }

    public BigInteger getQuery() {
        return query;
    }

    public BigInteger getRead() {
        return read;
    }

    public BigInteger getReadAll() {
        return readAll;
    }

    public Long getReadTime() {
        return readTime;
    }

    public DateTime getStart() {
        return start;
    }

    public BigInteger getSuccess() {
        return success;
    }

    public BigInteger getTotal() {
        return total;
    }

    public BigInteger getUpdate() {
        return update;
    }

    public Long getUpdateTime() {
        return updateTime;
    }
    
    private Map<String, String> generateCells(){
        Map<String, String> map = new HashMap<String, String>();
        
        map.put("uptime", getUptime());
        map.put("succeeded", getSuccess().toString());
        map.put("failed", getFail().toString());
        map.put("total", getTotal().toString());
        
        map.put("reads", getRead().toString());
        map.put("readalls", getReadAll().toString());
        map.put("inserts", getCreate().toString());
        map.put("updates", getUpdate().toString());
        map.put("deletes", getDelete().toString());
        
        map.put("read-time", getReadTime().toString());
        map.put("create-time", getCreateTime().toString());
        map.put("update-time", getUpdateTime().toString());
        map.put("delete-time", getDeleteTime().toString());
        
        return map;
    }
    
        
    public JongoResponse getUsageData(){
        Map<String, String> cells = generateCells();
        List<Row> rows = new ArrayList<Row>();
        rows.add(new Row(1, cells));
        JongoResponse res = new JongoSuccess("stats", rows);
        return res;
    }
}
