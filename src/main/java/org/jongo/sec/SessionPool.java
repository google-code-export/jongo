package org.jongo.sec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jongo.exceptions.TooManySessionsException;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class SessionPool {
    
    private final List<Session> sessions = Collections.synchronizedList(new ArrayList<Session>());
    private static final int max = 10;
    
    private SessionPool() {
    }
    
    public static SessionPool getInstance() {
        return SessionPoolHolder.INSTANCE;
    }
    
    private static class SessionPoolHolder {
        private static final SessionPool INSTANCE = new SessionPool();
    }
    
    public synchronized Session newSession() throws TooManySessionsException{
        if(sessions.size() > max){
            for(Session s : sessions){
                if(!s.isActive()){
                    sessions.remove(s);
                }
            }
            if(sessions.size() > max) 
                throw new TooManySessionsException("Reached the maximum number of sessions " + max); 
        }
        
        Session session = new Session("xxxxxxxx", 3000);
        sessions.add(session);
        return session;
    }
    
    public synchronized boolean isActive(Session session){
        if(session == null)
            throw new IllegalArgumentException("null is not a session");
        
        for(Session s : sessions){
            if(s.equals(session)){
                return s.isActive();
            }
        }
        
        throw new IllegalArgumentException("Invalid/Inexistent session");
    }
}
