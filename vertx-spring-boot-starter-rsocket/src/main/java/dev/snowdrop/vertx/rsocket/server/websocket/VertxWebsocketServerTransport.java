package dev.snowdrop.vertx.rsocket.server.websocket;

import dev.snowdrop.vertx.rsocket.server.VertxAbstractChannel;
import io.rsocket.DuplexConnection;
import io.rsocket.transport.ServerTransport;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.WebSocketBase;
import io.vertx.core.net.SocketAddress;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

public class VertxWebsocketServerTransport implements ServerTransport<VertxAbstractChannel> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HttpServer server;

    private VertxWebsocketServerTransport(HttpServer server) {
        this.server = Objects.requireNonNull(server, "server must not be null");
    }

    public static VertxWebsocketServerTransport create(HttpServer server) {
        return new VertxWebsocketServerTransport(server);
    }

    @NotNull
    @Override
    public Mono<VertxAbstractChannel> start(@NotNull ConnectionAcceptor acceptor) {
        Objects.requireNonNull(acceptor, "acceptor must not be null");

        server.requestHandler(request -> {
            String realIP = request.getHeader("X-Real-IP");
            SocketAddress address = request.remoteAddress();
            String IP = realIP == null ? address.toString() : realIP;
            logger.info("Upgrade {} {} {}", IP, request.method(), request.path());
            WebSocketBase webSocketBase = request.upgrade();
            DuplexConnection duplexConnection = new VertxWebsocketDuplexConnection(webSocketBase);
            acceptor.apply(duplexConnection).subscribe();
        });
        Mono
            .create(sink -> server.listen(result -> {
                if (result.succeeded()) {
                    logger.info("RSocket WebSocket transport on port {}", server.actualPort());
                    sink.success();
                } else {
                    sink.error(result.cause());
                }
            }))
            .block(Duration.ofSeconds(5));
        return Mono.just(new VertxHttpChannel(server));
    }
}
