package org.bigleg.async.parser;

import org.bigleg.async.DataEmitter;
import org.bigleg.async.DataSink;
import org.bigleg.async.callback.CompletedCallback;
import org.bigleg.async.future.Future;

import java.lang.reflect.Type;

/**
 * Created by koush on 5/27/13.
 */
public interface AsyncParser<T> {
    Future<T> parse(DataEmitter emitter);
    void write(DataSink sink, T value, CompletedCallback completed);
    Type getType();
}
