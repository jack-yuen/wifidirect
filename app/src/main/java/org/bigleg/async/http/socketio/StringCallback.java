package org.bigleg.async.http.socketio;

public interface StringCallback {
    public void onString(String string, Acknowledge acknowledge);
}