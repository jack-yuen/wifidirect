package org.bigleg.async.wrapper;

import org.bigleg.async.DataEmitter;

public interface DataEmitterWrapper extends DataEmitter {
    public DataEmitter getDataEmitter();
}
