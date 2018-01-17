package org.bigleg.async.http.server;

import org.bigleg.async.AsyncSocket;
import org.bigleg.async.DataEmitter;
import org.bigleg.async.FilteredDataEmitter;
import org.bigleg.async.LineEmitter;
import org.bigleg.async.LineEmitter.StringCallback;
import org.bigleg.async.callback.CompletedCallback;
import org.bigleg.async.callback.DataCallback;
import org.bigleg.async.http.Headers;
import org.bigleg.async.http.HttpUtil;
import org.bigleg.async.http.Protocol;
import org.bigleg.async.http.body.AsyncHttpRequestBody;

import java.util.regex.Matcher;

public abstract class AsyncHttpServerRequestImpl extends FilteredDataEmitter implements AsyncHttpServerRequest, CompletedCallback {
    private String statusLine;
    private Headers mRawHeaders = new Headers();
    AsyncSocket mSocket;
    Matcher mMatcher;

    public String getStatusLine() {
        return statusLine;
    }

    private CompletedCallback mReporter = new CompletedCallback() {
        @Override
        public void onCompleted(Exception error) {
            AsyncHttpServerRequestImpl.this.onCompleted(error);
        }
    };

    @Override
    public void onCompleted(Exception e) {
//        if (mBody != null)
//            mBody.onCompleted(e);
        report(e);
    }

    abstract protected void onHeadersReceived();
    
    protected void onNotHttp() {
        System.out.println("not http!");
    }

    protected AsyncHttpRequestBody onUnknownBody(Headers headers) {
        return null;
    }
    
    StringCallback mHeaderCallback = new StringCallback() {
        @Override
        public void onStringAvailable(String s) {
            try {
                if (statusLine == null) {
                    statusLine = s;
                    if (!statusLine.contains("HTTP/")) {
                        onNotHttp();
                        mSocket.setDataCallback(null);
                    }
                }
                else if (!"\r".equals(s)){
                    mRawHeaders.addLine(s);
                }
                else {
                    DataEmitter emitter = HttpUtil.getBodyDecoder(mSocket, Protocol.HTTP_1_1, mRawHeaders, true);
//                    emitter.setEndCallback(mReporter);
                    mBody = HttpUtil.getBody(emitter, mReporter, mRawHeaders);
                    if (mBody == null) {
                        mBody = onUnknownBody(mRawHeaders);
                        if (mBody == null)
                            mBody = new UnknownRequestBody(mRawHeaders.get("Content-Type"));
                    }
                    mBody.parse(emitter, mReporter);
                    onHeadersReceived();
                }
            }
            catch (Exception ex) {
                onCompleted(ex);
            }
        }
    };

    String method;
    @Override
    public String getMethod() {
        return method;
    }
    
    void setSocket(AsyncSocket socket) {
        mSocket = socket;

        LineEmitter liner = new LineEmitter();
        mSocket.setDataCallback(liner);
        liner.setLineCallback(mHeaderCallback);
        mSocket.setEndCallback(new NullCompletedCallback());
    }
    
    @Override
    public AsyncSocket getSocket() {
        return mSocket;
    }

    @Override
    public Headers getHeaders() {
        return mRawHeaders;
    }

    @Override
    public void setDataCallback(DataCallback callback) {
        mSocket.setDataCallback(callback);
    }

    @Override
    public DataCallback getDataCallback() {
        return mSocket.getDataCallback();
    }

    @Override
    public boolean isChunked() {
        return mSocket.isChunked();
    }

    @Override
    public Matcher getMatcher() {
        return mMatcher;
    }

    AsyncHttpRequestBody mBody;
    @Override
    public AsyncHttpRequestBody getBody() {
        return mBody;
    }

    @Override
    public void pause() {
        mSocket.pause();
    }

    @Override
    public void resume() {
        mSocket.resume();
    }

    @Override
    public boolean isPaused() {
        return mSocket.isPaused();
    }

    @Override
    public String toString() {
        if (mRawHeaders == null)
            return super.toString();
        return mRawHeaders.toPrefixString(statusLine);
    }
}
