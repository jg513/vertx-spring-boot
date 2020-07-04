package dev.snowdrop.vertx.rsocket.server.properties;

import org.springframework.boot.rsocket.server.RSocketServerFactory;

@FunctionalInterface
public interface RSocketFactoryCustomizer<T extends RSocketServerFactory> {

    void customize(T factory);
}
