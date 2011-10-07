package org.jongo.enums;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public enum ErrorCode {
    E200 ("No results"),
    E201 ("Invalid Session"),
    E202 ("Authentication Error"),
    E203 ("Invalid Operator"),
    E204 ("Failed to insert new registry"),
    E500 ("Application Error");
    
    private final String message;
    
    private ErrorCode(final String message){
        this.message = message;
    }
    
    public String getMessage(){
        return this.message;
    }
    
}
