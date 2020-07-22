package dev.snowdrop.vertx.reactor;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxImpl;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;

public class VertxScheduler implements Scheduler {

    private final VertxImpl vertx;

    private boolean disposed = false;

    public VertxScheduler(Vertx vertx) {
        this.vertx = (VertxImpl) vertx;
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
    public Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return new FutureDisposable(
            vertx.getEventLoopGroup().scheduleAtFixedRate(task, initialDelay, period, unit)
        );
    }

    @Override
    public Worker createWorker() {
        return new VertxWorker(vertx);
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
