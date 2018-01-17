package org.bigleg.async.callback;

import org.bigleg.async.future.Continuation;

public interface ContinuationCallback {
    public void onContinue(Continuation continuation, CompletedCallback next) throws Exception;
}
