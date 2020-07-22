package dev.snowdrop.vertx.reactor;

import reactor.core.Disposable;

import java.util.concurrent.Future;

public class FutureDisposable implements Disposable {

    private final Future<?> future;

    public FutureDisposable(Future<?> future) {
        this.future = future;
    }

    @Override
    public void dispose() {
        if (!future.isCancelled()) {
            future.cancel(false);
        }
    }

    @Override
    public boolean isDisposed() {
        return future.isDone();
    }
}
