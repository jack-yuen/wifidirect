package org.bigleg.async.callback;

import org.bigleg.async.AsyncSocket;

public interface ConnectCallback {
    public void onConnectCompleted(Exception ex, AsyncSocket socket);
}
