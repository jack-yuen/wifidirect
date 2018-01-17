package org.bigleg.async.parser;

import org.bigleg.async.ByteBufferList;
import org.bigleg.async.DataEmitter;
import org.bigleg.async.DataSink;
import org.bigleg.async.callback.CompletedCallback;
import org.bigleg.async.future.Future;
import org.bigleg.async.future.TransformFuture;
import org.bigleg.async.http.body.DocumentBody;
import org.bigleg.async.stream.ByteBufferListInputStream;

import org.w3c.dom.Document;

import java.lang.reflect.Type;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by koush on 8/3/13.
 */
public class DocumentParser implements AsyncParser<Document> {
    @Override
    public Future<Document> parse(DataEmitter emitter) {
        return new ByteBufferListParser().parse(emitter)
        .then(new TransformFuture<Document, ByteBufferList>() {
            @Override
            protected void transform(ByteBufferList result) throws Exception {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                setComplete(db.parse(new ByteBufferListInputStream(result)));
            }
        });
    }

    @Override
    public void write(DataSink sink, Document value, CompletedCallback completed) {
        new DocumentBody(value).write(null, sink, completed);
    }

    @Override
    public Type getType() {
        return Document.class;
    }
}
