package dev.snowdrop.vertx.rsocket.server.tcp;

import dev.snowdrop.vertx.rsocket.server.VertxAbstractChannel;
import io.vertx.core.net.NetServer;

import java.net.InetSocketAddress;

public class VertxTcpChannel extends VertxAbstractChannel {

    private final NetServer netServer;

    public VertxTcpChannel(NetServer netServer) {
        this.netServer = netServer;
    }

    public InetSocketAddress address() {
        return new InetSocketAddress(netServer.actualPort());
    }
}
