package org.bigleg.async.http.body;

import org.bigleg.async.ByteBufferList;
import org.bigleg.async.DataEmitter;
import org.bigleg.async.DataSink;
import org.bigleg.async.Util;
import org.bigleg.async.callback.CompletedCallback;
import org.bigleg.async.callback.DataCallback;
import org.bigleg.async.http.AsyncHttpRequest;
import org.bigleg.async.http.Multimap;
import org.bigleg.async.http.NameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class UrlEncodedFormBody implements AsyncHttpRequestBody<Multimap> {
    private Multimap mParameters;
    private byte[] mBodyBytes;

    public UrlEncodedFormBody(Multimap parameters) {
        mParameters = parameters;
    }

    public UrlEncodedFormBody(List<NameValuePair> parameters) {
        mParameters = new Multimap(parameters);
    }

    private void buildData() {
        boolean first = true;
        StringBuilder b = new StringBuilder();
        try {
            for (NameValuePair pair: mParameters) {
                if (pair.getValue() == null)
                    continue;
                if (!first)
                    b.append('&');
                first = false;

                b.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                b.append('=');
                b.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            }
            mBodyBytes = b.toString().getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
    
    @Override
    public void write(AsyncHttpRequest request, final DataSink response, final CompletedCallback completed) {
        if (mBodyBytes == null)
            buildData();
        Util.writeAll(response, mBodyBytes, completed);
    }

    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    @Override
    public String getContentType() {
        return CONTENT_TYPE + "; charset=utf-8";
    }

    @Override
    public void parse(DataEmitter emitter, final CompletedCallback completed) {
        final ByteBufferList data = new ByteBufferList();
        emitter.setDataCallback(new DataCallback() {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                bb.get(data);
            }
        });
        emitter.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) {
                    completed.onCompleted(ex);
                    return;
                }
                try {
                    mParameters = Multimap.parseUrlEncoded(data.readString());
                    completed.onCompleted(null);
                }
                catch (Exception e) {
                    completed.onCompleted(e);
                }
            }
        });
    }

    public UrlEncodedFormBody() {
    }

    @Override
    public boolean readFullyOnRequest() {
        return true;
    }

    @Override
    public int length() {
        if (mBodyBytes == null)
            buildData();
        return mBodyBytes.length;
    }

    @Override
    public Multimap get() {
        return mParameters;
    }
}
