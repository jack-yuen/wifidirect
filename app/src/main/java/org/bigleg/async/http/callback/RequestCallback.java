package org.bigleg.async.http.callback;

import org.bigleg.async.callback.ResultCallback;
import org.bigleg.async.http.AsyncHttpResponse;

public interface RequestCallback<T> extends ResultCallback<AsyncHttpResponse, T> {
    public void onConnect(AsyncHttpResponse response);
    public void onProgress(AsyncHttpResponse response, long downloaded, long total);
}
