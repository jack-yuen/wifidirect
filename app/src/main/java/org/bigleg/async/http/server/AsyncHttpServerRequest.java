package org.bigleg.async.http.server;

import org.bigleg.async.AsyncSocket;
import org.bigleg.async.DataEmitter;
import org.bigleg.async.http.Headers;
import org.bigleg.async.http.Multimap;
import org.bigleg.async.http.body.AsyncHttpRequestBody;

import java.util.regex.Matcher;

public interface AsyncHttpServerRequest extends DataEmitter {
    public Headers getHeaders();
    public Matcher getMatcher();
    public AsyncHttpRequestBody getBody();
    public AsyncSocket getSocket();
    public String getPath();
    public Multimap getQuery();
    public String getMethod();
}
