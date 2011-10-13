package org.jongo.enums;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public enum Permission {
    NONE,
    READ,
    WRITE,
    READWRITE;
    
    public static Permission valueOf(int value){
        switch(value){
            case 1:
                return READ;
            case 2:
                return WRITE;
            case 3:
                return READWRITE;
            default:
                return NONE;
        }
    }
    
    public int getValue(){
        return this.ordinal();
    }
    
    public boolean isReadable(){
        return (this == READ || this == READWRITE);
    }
    
    public boolean isWritable(){
        return (this == WRITE || this == READWRITE);
    }
}
