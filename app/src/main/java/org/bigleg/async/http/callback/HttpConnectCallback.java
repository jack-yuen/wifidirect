package org.bigleg.async.http.callback;


import org.bigleg.async.http.AsyncHttpResponse;

public interface HttpConnectCallback {
    public void onConnectCompleted(Exception ex, AsyncHttpResponse response);
}
