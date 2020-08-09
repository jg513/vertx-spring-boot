package dev.snowdrop.vertx.rsocket.server;

import io.rsocket.Closeable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

public abstract class VertxAbstractChannel implements Closeable {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    @Override
    public Mono<Void> onClose() {
        logger.info("onClose");
        return Mono.empty();
    }

    @Override
    public void dispose() {
        logger.info("dispose");
    }

    public abstract InetSocketAddress address();
}
