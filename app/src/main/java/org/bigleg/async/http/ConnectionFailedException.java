package org.bigleg.async.http;

public class ConnectionFailedException extends Exception {
    public ConnectionFailedException(String message) {
        super(message);
    }
}
