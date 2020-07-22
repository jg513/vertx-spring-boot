package dev.snowdrop.vertx.reactor;

import io.vertx.core.impl.VertxImpl;
import reactor.core.Disposable;

import java.util.concurrent.TimeUnit;

import static reactor.core.scheduler.Scheduler.Worker;

public class VertxWorker implements Worker {

    private final VertxImpl vertx;

    private boolean disposed = false;

    public VertxWorker(VertxImpl vertx) {
        this.vertx = vertx;
    }

    @Override
    public Disposable schedule(Runnable task) {
        Disposable disposable = new BooleanDisposable();
        vertx.runOnContext(event -> {
            if (!disposable.isDisposed()) {
                task.run();
            }
        });
        return disposable;
    }

    @Override
    public Disposable schedule(Runnable task, long delay, TimeUnit unit) {
        return new FutureDisposable(vertx.getEventLoopGroup().schedule(task, delay, unit));
    }

    @Override
    public Disposable schedulePeriodically(Runnable task,
                                           long initialDelay,
                                           long period,
                                           TimeUnit unit) {
        return new FutureDisposable(
            vertx.getEventLoopGroup().scheduleAtFixedRate(task, initialDelay, period, unit)
        );
    }

    @Override
    public void dispose() {
        this.disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
