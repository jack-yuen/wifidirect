package org.bigleg.async.wrapper;

import org.bigleg.async.AsyncSocket;

public interface AsyncSocketWrapper extends AsyncSocket, DataEmitterWrapper {
    public AsyncSocket getSocket();
}
