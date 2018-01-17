package org.bigleg.async.parser;

import org.bigleg.async.DataEmitter;
import org.bigleg.async.DataSink;
import org.bigleg.async.callback.CompletedCallback;
import org.bigleg.async.future.Future;
import org.bigleg.async.future.TransformFuture;
import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * Created by koush on 5/27/13.
 */
public class JSONObjectParser implements AsyncParser<JSONObject> {
    @Override
    public Future<JSONObject> parse(DataEmitter emitter) {
        return new StringParser().parse(emitter)
        .then(new TransformFuture<JSONObject, String>() {
            @Override
            protected void transform(String result) throws Exception {
                setComplete(new JSONObject(result));
            }
        });
    }

    @Override
    public void write(DataSink sink, JSONObject value, CompletedCallback completed) {
        new StringParser().write(sink, value.toString(), completed);
    }

    @Override
    public Type getType() {
        return JSONObject.class;
    }
}
