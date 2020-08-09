package dev.snowdrop.vertx.rsocket.server.tcp;

import dev.snowdrop.vertx.rsocket.server.VertxAbstractChannel;
import io.rsocket.DuplexConnection;
import io.rsocket.transport.ServerTransport;
import io.vertx.core.net.NetServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

public class VertxTcpServerTransport implements ServerTransport<VertxAbstractChannel> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NetServer server;

    private VertxTcpServerTransport(NetServer server) {
        this.server = Objects.requireNonNull(server, "server must not be null");
    }

    public static VertxTcpServerTransport create(NetServer server) {
        return new VertxTcpServerTransport(server);
    }

    @NotNull
    @Override
    public Mono<VertxAbstractChannel> start(@NotNull ConnectionAcceptor acceptor) {
        Objects.requireNonNull(acceptor, "acceptor must not be null");

        server.connectHandler(netSocket -> {
            DuplexConnection duplexConnection = new VertxTcpDuplexConnection(netSocket);
            acceptor.apply(duplexConnection).subscribe();
        });
        Mono
            .create(sink -> server.listen(result -> {
                if (result.succeeded()) {
                    logger.info("RSocket Tcp transport on port {}", server.actualPort());
                    sink.success();
                } else {
                    sink.error(result.cause());
                }
            }))
            .block(Duration.ofSeconds(5));
        return Mono.just(new VertxTcpChannel(server));
    }
}
