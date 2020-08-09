package dev.snowdrop.vertx.rsocket.server;

import dev.snowdrop.vertx.rsocket.server.properties.AbstractConfigurableRSocketServerFactory;
import dev.snowdrop.vertx.rsocket.server.properties.HttpServerOptionsCustomizer;
import dev.snowdrop.vertx.rsocket.server.properties.HttpServerProperties;
import dev.snowdrop.vertx.rsocket.server.tcp.VertxTcpServerTransport;
import dev.snowdrop.vertx.rsocket.server.websocket.VertxWebsocketServerTransport;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.ServerTransport;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.NetServer;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.boot.rsocket.server.RSocketServer.Transport.TCP;

public class VertxRSocketServerFactory extends AbstractConfigurableRSocketServerFactory {

    private final Vertx vertx;

    private final HttpServerProperties properties;

    private final List<HttpServerOptionsCustomizer> httpServerOptionsCustomizers = new LinkedList<>();

    private List<RSocketServerCustomizer> rSocketServerCustomizers = new ArrayList<>();

    private Duration lifecycleTimeout;

    public VertxRSocketServerFactory(Vertx vertx, HttpServerProperties properties) {
        this.vertx = vertx;
        this.properties = properties;
    }

    public void setRSocketServerCustomizers(Collection<? extends RSocketServerCustomizer> rSocketServerCustomizers) {
        Assert.notNull(rSocketServerCustomizers, "RSocketServerCustomizers must not be null");
        this.rSocketServerCustomizers = new ArrayList<>(rSocketServerCustomizers);
    }

    public void setLifecycleTimeout(Duration lifecycleTimeout) {
        this.lifecycleTimeout = lifecycleTimeout;
    }

    @Override
    public VertxRSocketServer create(SocketAcceptor socketAcceptor) {
        ServerTransport<VertxAbstractChannel> transport = createTransport();
        RSocketServer server = RSocketServer.create(socketAcceptor);
        this.rSocketServerCustomizers.forEach((customizer) -> customizer.customize(server));
        Mono<VertxAbstractChannel> starter = server.bind(transport);
        return new VertxRSocketServer(starter, this.lifecycleTimeout);
    }

    public void registerHttpServerOptionsCustomizer(HttpServerOptionsCustomizer customizer) {
        httpServerOptionsCustomizers.add(customizer);
    }

    private HttpServerOptions customizeHttpServerOptions(HttpServerOptions httpServerOptions) {
        for (HttpServerOptionsCustomizer customizer : httpServerOptionsCustomizers) {
            httpServerOptions = customizer.apply(httpServerOptions);
        }
        return httpServerOptions;
    }

    private ServerTransport<VertxAbstractChannel> createTransport() {
        return this.transport == TCP ? createTcpTransport() : createWebSocketTransport();
    }

    private ServerTransport<VertxAbstractChannel> createWebSocketTransport() {
        HttpServerOptions options = customizeHttpServerOptions(properties.getHttpServerOptions());
        HttpServer httpServer = vertx.createHttpServer(options);
        return VertxWebsocketServerTransport.create(httpServer);
    }

    private ServerTransport<VertxAbstractChannel> createTcpTransport() {
        HttpServerOptions options = customizeHttpServerOptions(properties.getHttpServerOptions());
        NetServer netServer = vertx.createNetServer(options);
        return VertxTcpServerTransport.create(netServer);
    }
}
