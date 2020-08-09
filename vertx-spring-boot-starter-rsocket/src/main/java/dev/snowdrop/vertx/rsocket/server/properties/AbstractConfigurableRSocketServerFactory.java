package dev.snowdrop.vertx.rsocket.server.properties;

import org.springframework.boot.rsocket.server.ConfigurableRSocketServerFactory;
import org.springframework.boot.rsocket.server.RSocketServerFactory;

import java.net.InetAddress;

import static org.springframework.boot.rsocket.server.RSocketServer.Transport;

public abstract class AbstractConfigurableRSocketServerFactory implements RSocketServerFactory, ConfigurableRSocketServerFactory {

    protected int port;

    protected InetAddress address;

    protected Transport transport = Transport.TCP;

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
    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public Transport getTransport() {
        return transport;
    }
}
