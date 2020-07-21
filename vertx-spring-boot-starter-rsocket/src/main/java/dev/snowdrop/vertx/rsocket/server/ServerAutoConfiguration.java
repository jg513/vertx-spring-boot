package dev.snowdrop.vertx.rsocket.server;

import dev.snowdrop.vertx.rsocket.server.properties.HttpServerOptionsCustomizer;
import dev.snowdrop.vertx.rsocket.server.properties.HttpServerProperties;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.rsocket.context.RSocketServerBootstrap;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.boot.rsocket.server.RSocketServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty(prefix = "spring.rsocket.server", name = "port")
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnMissingBean(RSocketServerFactory.class)
@EnableConfigurationProperties({ RSocketProperties.class, HttpServerProperties.class })
public class ServerAutoConfiguration {

    @Bean
    public RSocketServerFactory rSocketServerFactory(RSocketProperties properties,
                                                     Vertx vertx,
                                                     HttpServerProperties serverProperties,
                                                     ObjectProvider<RSocketServerCustomizer> customizers,
                                                     Set<HttpServerOptionsCustomizer> userDefinedCustomizers) {
        VertxRSocketServerFactory factory = new VertxRSocketServerFactory(vertx, serverProperties);
        factory.setTransport(properties.getServer().getTransport());
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(properties.getServer().getAddress()).to(factory::setAddress);
        map.from(properties.getServer().getPort()).to(factory::setPort);
        factory.setRSocketServerCustomizers(customizers.orderedStream().collect(Collectors.toList()));
        vertxRSocketServerFactoryCustomizer(userDefinedCustomizers).customize(factory);
        return factory;
    }

    @Bean
    public VertxRSocketServerFactoryCustomizer vertxRSocketServerFactoryCustomizer(
        Set<HttpServerOptionsCustomizer> userDefinedCustomizers) {
        return new VertxRSocketServerFactoryCustomizer(userDefinedCustomizers);
    }

    @Bean
    @ConditionalOnMissingBean
    RSocketServerBootstrap rSocketServerBootstrap(RSocketServerFactory rSocketServerFactory,
                                                  RSocketMessageHandler rSocketMessageHandler) {
        return new RSocketServerBootstrap(rSocketServerFactory, rSocketMessageHandler.responder());
    }
}
