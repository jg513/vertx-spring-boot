package dev.snowdrop.vertx.rsocket.server.properties;

import io.vertx.core.http.HttpServerOptions;

import java.net.InetAddress;

public class AddressCustomizer implements HttpServerOptionsCustomizer {

    private final AbstractConfigurableRSocketServerFactory factory;

    public AddressCustomizer(AbstractConfigurableRSocketServerFactory factory) {
        this.factory = factory;
    }

    @Override
    public HttpServerOptions apply(HttpServerOptions options) {
        InetAddress address = factory.getAddress();

        if (address != null && address.getHostAddress() != null) {
            options.setHost(address.getHostAddress());
        }

        return options;
    }
}
