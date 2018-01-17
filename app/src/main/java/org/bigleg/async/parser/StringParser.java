package org.bigleg.async.parser;

import org.bigleg.async.ByteBufferList;
import org.bigleg.async.DataEmitter;
import org.bigleg.async.DataSink;
import org.bigleg.async.callback.CompletedCallback;
import org.bigleg.async.future.Future;
import org.bigleg.async.future.TransformFuture;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * Created by koush on 5/27/13.
 */
public class StringParser implements AsyncParser<String> {
    Charset forcedCharset;

    public StringParser() {
    }

    public StringParser(Charset charset) {
        this.forcedCharset = charset;
    }

    @Override
    public Future<String> parse(DataEmitter emitter) {
        final String charset = emitter.charset();
        return new ByteBufferListParser().parse(emitter)
        .then(new TransformFuture<String, ByteBufferList>() {
            @Override
            protected void transform(ByteBufferList result) throws Exception {
                Charset charsetToUse = forcedCharset;
                if (charsetToUse == null && charset != null)
                    charsetToUse = Charset.forName(charset);
                setComplete(result.readString(charsetToUse));
            }
        });
    }

    @Override
    public void write(DataSink sink, String value, CompletedCallback completed) {
        new ByteBufferListParser().write(sink, new ByteBufferList(value.getBytes()), completed);
    }

    @Override
    public Type getType() {
        return String.class;
    }
}
