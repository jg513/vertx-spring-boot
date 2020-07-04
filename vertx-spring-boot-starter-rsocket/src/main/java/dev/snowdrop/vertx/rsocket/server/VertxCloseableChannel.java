package dev.snowdrop.vertx.rsocket.server;

import io.rsocket.Closeable;
import io.vertx.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.net.InetSocketAddress;

public class VertxCloseableChannel implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HttpServer httpServer;

    public VertxCloseableChannel(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

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

    public InetSocketAddress address() {
        return new InetSocketAddress(httpServer.actualPort());
    }
}
