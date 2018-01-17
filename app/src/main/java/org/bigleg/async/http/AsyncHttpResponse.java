package org.bigleg.async.http;

import org.bigleg.async.AsyncSocket;
import org.bigleg.async.DataEmitter;

public interface AsyncHttpResponse extends DataEmitter {
    public String protocol();
    public String message();
    public int code();
    public Headers headers();
    public AsyncSocket detachSocket();
    public AsyncHttpRequest getRequest();
}
