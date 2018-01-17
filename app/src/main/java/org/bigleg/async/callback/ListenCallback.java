package org.bigleg.async.callback;

import org.bigleg.async.AsyncServerSocket;
import org.bigleg.async.AsyncSocket;


public interface ListenCallback extends CompletedCallback {
    public void onAccepted(AsyncSocket socket);
    public void onListening(AsyncServerSocket socket);
}
