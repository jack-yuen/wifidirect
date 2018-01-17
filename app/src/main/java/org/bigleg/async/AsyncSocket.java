package org.bigleg.async;


public interface AsyncSocket extends DataEmitter, DataSink {
    public AsyncServer getServer();
}
