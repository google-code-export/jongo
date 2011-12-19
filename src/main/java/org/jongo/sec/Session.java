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

import java.util.Date;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class Session {
    
    private final String id;
    private final Date startTime = new Date();
    private Date lastAccess = new Date();
    private final long timeToLive;

    public Session(String id, long timeToLive) {
        this.id = id;
        this.timeToLive = timeToLive;
    }

    public String getId() {
        return id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    public void updateSession(){
        this.lastAccess = new Date();
    }
    
    public boolean isActive(){
        final long diff = lastAccess.getTime() - startTime.getTime();
        return diff < timeToLive;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Session other = (Session) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.startTime != null ? this.startTime.hashCode() : 0);
        hash = 53 * hash + (this.lastAccess != null ? this.lastAccess.hashCode() : 0);
        hash = 53 * hash + (int) (this.timeToLive ^ (this.timeToLive >>> 32));
        return hash;
    }
}
