package dev.snowdrop.vertx.rsocket.server.websocket;

import dev.snowdrop.vertx.rsocket.server.VertxAbstractChannel;
import io.vertx.core.http.HttpServer;

import java.net.InetSocketAddress;

public class VertxHttpChannel extends VertxAbstractChannel {

    private final HttpServer httpServer;

    public VertxHttpChannel(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    public InetSocketAddress address() {
        return new InetSocketAddress(httpServer.actualPort());
    }
}
