package org.jongo.exceptions;

/**
 *
 * @author Alejandro Ayuso <alejandroayuso@gmail.com>
 */
public class TooManySessionsException extends Exception {

    /**
     * Creates a new instance of <code>TooManySessionsException</code> without detail message.
     */
    public TooManySessionsException() {
    }

    /**
     * Constructs an instance of <code>TooManySessionsException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TooManySessionsException(String msg) {
        super(msg);
    }
}
