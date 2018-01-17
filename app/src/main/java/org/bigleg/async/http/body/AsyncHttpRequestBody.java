package org.bigleg.async.http.body;

import org.bigleg.async.DataEmitter;
import org.bigleg.async.DataSink;
import org.bigleg.async.callback.CompletedCallback;
import org.bigleg.async.http.AsyncHttpRequest;

public interface AsyncHttpRequestBody<T> {
    public void write(AsyncHttpRequest request, DataSink sink, CompletedCallback completed);
    public void parse(DataEmitter emitter, CompletedCallback completed);
    public String getContentType();
    public boolean readFullyOnRequest();
    public int length();
    public T get();
}
