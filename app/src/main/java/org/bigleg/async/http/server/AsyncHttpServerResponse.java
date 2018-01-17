package org.bigleg.async.http.server;

import org.bigleg.async.AsyncSocket;
import org.bigleg.async.DataSink;
import org.bigleg.async.callback.CompletedCallback;
import org.bigleg.async.http.AsyncHttpResponse;
import org.bigleg.async.http.Headers;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;

public interface AsyncHttpServerResponse extends DataSink, CompletedCallback {
    public void end();
    public void send(String contentType, byte[] bytes);
    public void send(String contentType, String string);
    public void send(String string);
    public void send(JSONObject json);
    public void sendFile(File file);
    public void sendStream(InputStream inputStream, long totalLength);
    public AsyncHttpServerResponse code(int code);
    public int code();
    public Headers getHeaders();
    public void writeHead();
    public void setContentType(String contentType);
    public void redirect(String location);

    // NOT FINAL
    public void proxy(AsyncHttpResponse response);

    /**
     * Alias for end. Used with CompletedEmitters
     */
    public void onCompleted(Exception ex);
    public AsyncSocket getSocket();
}
