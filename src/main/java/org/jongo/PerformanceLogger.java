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

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceLogger {
    private static final Logger l = LoggerFactory.getLogger(PerformanceLogger.class);
    
    private final Code code;
    private final String msg;
    private Long start;
    private Long end;
    
    private PerformanceLogger(final Code code, final String msg){
        this.code = code;
        this.msg = msg;
    }
    
    public static PerformanceLogger start(final Code code){
        PerformanceLogger instance = new PerformanceLogger(code, "");
        instance.start = System.nanoTime();
        return instance;
    }
    
    public static PerformanceLogger start(final Code code, final String msg){
        PerformanceLogger instance = new PerformanceLogger(code, msg);
        instance.start = System.nanoTime();
        return instance;
    }
    
    public long end(){
        this.end = System.nanoTime();
        final long dur = TimeUnit.NANOSECONDS.toMillis(this.end - this.start);
        StringBuilder b = new StringBuilder("[");
        if(msg.length() > 0){
            b.append(msg);
            b.append(":");
        }
        b.append(code);
        b.append(":");
        b.append(dur);
        b.append("]");
        l.debug(b.toString());
        return dur;
    }
    
    public enum Code{
        UNK,
        DBMETA,
        RSMETA,
        READ,
        READALL,
        CREATE,
        UPDATE,
        DELETE;
        
    }
}
