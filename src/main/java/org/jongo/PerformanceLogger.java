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
    
    private PerformanceLogger(){
        this.code = Code.UNK;
        this.msg = "";
    }
    
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
    
    public void end(){
        this.end = System.nanoTime();
        StringBuilder b = new StringBuilder("[");
        if(msg.length() > 0){
            b.append(msg);
            b.append(":");
        }
        b.append(code);
        b.append(":");
        b.append(TimeUnit.NANOSECONDS.toMillis(this.end - this.start));
        b.append("]");
        l.debug(b.toString());
    }
    
    public enum Code{
        UNK,
        DBMETA,
        RSMETA,
        READ,
        CREATE,
        UPDATE,
        DELETE;
        
    }
}
