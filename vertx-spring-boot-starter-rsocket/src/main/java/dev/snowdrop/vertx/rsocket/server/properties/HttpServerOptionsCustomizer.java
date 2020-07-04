package dev.snowdrop.vertx.rsocket.server.properties;

import io.vertx.core.http.HttpServerOptions;

import java.util.function.Function;

@FunctionalInterface
public interface HttpServerOptionsCustomizer extends Function<HttpServerOptions, HttpServerOptions> {

}
