package org.bigleg.async.callback;

import org.bigleg.async.ByteBufferList;
import org.bigleg.async.DataEmitter;


public interface DataCallback {
    public class NullDataCallback implements DataCallback {
        @Override
        public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
            bb.recycle();
        }
    }

    public void onDataAvailable(DataEmitter emitter, ByteBufferList bb);
}
