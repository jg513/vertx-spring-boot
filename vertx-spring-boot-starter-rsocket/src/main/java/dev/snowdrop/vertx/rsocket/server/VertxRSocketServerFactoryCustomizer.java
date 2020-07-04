package dev.snowdrop.vertx.rsocket.server;

import dev.snowdrop.vertx.rsocket.server.properties.AddressCustomizer;
import dev.snowdrop.vertx.rsocket.server.properties.HttpServerOptionsCustomizer;
import dev.snowdrop.vertx.rsocket.server.properties.PortCustomizer;
import dev.snowdrop.vertx.rsocket.server.properties.RSocketFactoryCustomizer;
import org.springframework.core.Ordered;

import java.util.Set;

public class VertxRSocketServerFactoryCustomizer
    implements RSocketFactoryCustomizer<VertxRSocketServerFactory>, Ordered {

    private final Set<HttpServerOptionsCustomizer> userDefinedCustomizers;

    public VertxRSocketServerFactoryCustomizer(Set<HttpServerOptionsCustomizer> userDefinedCustomizers) {
        this.userDefinedCustomizers = userDefinedCustomizers;
    }

    @Override
    public void customize(VertxRSocketServerFactory factory) {
        factory.registerHttpServerOptionsCustomizer(new PortCustomizer(factory));
        factory.registerHttpServerOptionsCustomizer(new AddressCustomizer(factory));

        if (userDefinedCustomizers != null) {
            userDefinedCustomizers.forEach(factory::registerHttpServerOptionsCustomizer);
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
