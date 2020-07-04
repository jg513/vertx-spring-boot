package dev.snowdrop.vertx.rsocket.server;

import dev.snowdrop.vertx.rsocket.server.properties.AbstractConfigurableRSocketServerFactory;
import dev.snowdrop.vertx.rsocket.server.properties.HttpServerOptionsCustomizer;
import dev.snowdrop.vertx.rsocket.server.properties.HttpServerProperties;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.transport.ServerTransport;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import org.springframework.boot.rsocket.server.RSocketServer;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

public class VertxRSocketServerFactory extends AbstractConfigurableRSocketServerFactory {

    private final Vertx vertx;

    private final HttpServerProperties properties;

    private final List<HttpServerOptionsCustomizer> httpServerOptionsCustomizers = new LinkedList<>();

    private RSocketServer.Transport transport = RSocketServer.Transport.TCP;

    private Duration lifecycleTimeout;

    public VertxRSocketServerFactory(Vertx vertx, HttpServerProperties properties) {
        this.vertx = vertx;
        this.properties = properties;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    @Override
    public void setTransport(RSocketServer.Transport transport) {
        this.transport = transport;
    }

    public void setLifecycleTimeout(Duration lifecycleTimeout) {
        this.lifecycleTimeout = lifecycleTimeout;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VertxRSocketServer create(SocketAcceptor socketAcceptor) {
        ServerTransport<VertxCloseableChannel> transport = createTransport();
        io.rsocket.core.RSocketServer server = io.rsocket.core.RSocketServer.create(socketAcceptor);
        RSocketFactory.ServerRSocketFactory factory = new RSocketFactory.ServerRSocketFactory(server);
        Mono<VertxCloseableChannel> starter = factory.transport(transport).start();
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

    private ServerTransport<VertxCloseableChannel> createTransport() {
        if (this.transport == RSocketServer.Transport.WEBSOCKET) {
            return createWebSocketTransport();
        }
        return createTcpTransport();
    }

    private ServerTransport<VertxCloseableChannel> createWebSocketTransport() {
        HttpServerOptions options = customizeHttpServerOptions(properties.getHttpServerOptions());
        HttpServer httpServer = vertx.createHttpServer(options);
        return VertxWebsocketServerTransport.create(httpServer);
    }

    private ServerTransport<VertxCloseableChannel> createTcpTransport() {
        throw new RuntimeException("Tcp transport not supported");
    }
}
