package org.bigleg.async.http.filter;

public class PrematureDataEndException extends Exception {
    public PrematureDataEndException(String message) {
        super(message);
    }
}
