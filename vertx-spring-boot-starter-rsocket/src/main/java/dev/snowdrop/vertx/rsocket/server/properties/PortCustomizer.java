package dev.snowdrop.vertx.rsocket.server.properties;

import io.vertx.core.http.HttpServerOptions;

public class PortCustomizer implements HttpServerOptionsCustomizer {

    private final AbstractConfigurableRSocketServerFactory factory;

    public PortCustomizer(AbstractConfigurableRSocketServerFactory factory) {
        this.factory = factory;
    }

    @Override
    public HttpServerOptions apply(HttpServerOptions options) {
        if (factory.getPort() >= 0) {
            options.setPort(factory.getPort());
        }

        return options;
    }

}
