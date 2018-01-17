package org.bigleg.async.future;

public interface DependentFuture<T> extends Future<T>, DependentCancellable {
}
