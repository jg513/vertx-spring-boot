package dev.snowdrop.vertx.rsocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.boot.rsocket.server.RSocketServerException;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;

public class VertxRSocketServer implements RSocketServer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Mono<VertxAbstractChannel> starter;

    private final Duration lifecycleTimeout;

    private VertxAbstractChannel channel;

    public VertxRSocketServer(Mono<VertxAbstractChannel> starter, Duration lifecycleTimeout) {
        this.starter = starter;
        this.lifecycleTimeout = lifecycleTimeout;
    }

    @Override
    public void start() throws RSocketServerException {
        logger.info("Vertx RSocket server started");
        this.channel = block(this.starter, this.lifecycleTimeout);
    }

    @Override
    public void stop() throws RSocketServerException {
        logger.info("Vertx RSocket server stopped");
        if (this.channel != null) {
            this.channel.dispose();
            this.channel = null;
        }
    }

    @Override
    public InetSocketAddress address() {
        return channel == null ? null : channel.address();
    }

    private <T> T block(Mono<T> mono, Duration timeout) {
        return (timeout != null) ? mono.block(timeout) : mono.block();
    }
}
