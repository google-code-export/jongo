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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.jongo.JongoUtils;

/**
 * A singleton which holds usage data for the current running instance.
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Usage  implements JongoResponse{
    
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
    
    @Override
    public String toJSON(){
        StringBuilder b = new StringBuilder("{");
        b.append("\"success\":");b.append(isSuccess());
        b.append(",\"response\":{");
        b.append("\"total\":");b.append(total);
        b.append(",\"success\":");b.append(success);
        b.append(",\"fail\":");b.append(fail);
        b.append(",\"create\":");b.append(create);
        b.append(",\"read\":");b.append(read);
        b.append(",\"update\":");b.append(update);
        b.append(",\"delete\":");b.append(delete);
        b.append(",\"createTime\":");b.append(createTime);
        b.append(",\"readTime\":");b.append(readTime);
        b.append(",\"updateTime\":");b.append(updateTime);
        b.append(",\"deleteTime\":");b.append(deleteTime);
        b.append(",\"dynamic\":");b.append(dynamic);
        b.append(",\"query\":");b.append(query);
        b.append("}}");
        return b.toString();
    }
    
    @Override
    public String getResource() {
        return "stats";
    }

    @Override
    public Status getStatus() {
        return Response.Status.OK;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public String toXML() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Response getResponse(MediaType format) {
        String response = (format.isCompatible(MediaType.valueOf(MediaType.APPLICATION_XML))) ? this.toXML() : this.toJSON();
        String md5sum = JongoUtils.getMD5Base64(response);
        Integer length = JongoUtils.getOctetLength(response);
        return Response.status(getStatus())
                .entity(response)
                .type(format)
                .header("Date", JongoUtils.getDateHeader())
                .header("Content-MD5", md5sum)
                .header("Content-Length", length)
                .header("Content-Location", "jongo")
                .build();
    }
}
