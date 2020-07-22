package dev.snowdrop.vertx.reactor;

import reactor.core.Disposable;

public class BooleanDisposable implements Disposable {

    private volatile boolean disposed;

    @Override
    public void dispose() {
        this.disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
