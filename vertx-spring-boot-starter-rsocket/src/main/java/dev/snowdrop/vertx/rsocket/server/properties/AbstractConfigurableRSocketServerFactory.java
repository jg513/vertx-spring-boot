package dev.snowdrop.vertx.rsocket.server.properties;

import org.springframework.boot.rsocket.server.ConfigurableRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServer;
import org.springframework.boot.rsocket.server.RSocketServerFactory;

import java.net.InetAddress;

public abstract class AbstractConfigurableRSocketServerFactory implements RSocketServerFactory, ConfigurableRSocketServerFactory {

    protected int port;

    protected InetAddress address;

    protected RSocketServer.Transport transport;

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public InetAddress getAddress() {
        return address;
    }

    @Override
    public void setTransport(RSocketServer.Transport transport) {
        this.transport = transport;
    }

    public RSocketServer.Transport getTransport() {
        return transport;
    }
}
