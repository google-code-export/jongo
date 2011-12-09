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
