package dev.snowdrop.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.spi.cluster.ClusterManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Configuration
@ConditionalOnClass(Vertx.class)
@EnableConfigurationProperties(VertxProperties.class)
public class VertxAutoConfiguration {

    @ConditionalOnBean(ClusterManager.class)
    @ConditionalOnMissingBean(Vertx.class)
    @Bean(destroyMethod = "")
    public Vertx vertxCluster(VertxProperties properties, ClusterManager clusterManager) throws Exception {
        CompletableFuture<Vertx> future = new CompletableFuture<>();
        Vertx.clusteredVertx(
            properties.toVertxOptions().setClusterManager(clusterManager),
            event -> {
                if (event.succeeded()) {
                    future.complete(event.result());
                } else {
                    future.completeExceptionally(event.cause());
                }
            }
        );
        return future.get();
    }

    // Let the Vertx user to handle instance closing.
    // This is done in particular for HTTP server which is closed by Spring Context after beans are destroyed.
    // Allowing Vertx bean to be destroyed by the context would block HTTP server from calling its close method.
    @ConditionalOnMissingBean({ Vertx.class, ClusterManager.class })
    @Bean(destroyMethod = "")
    public Vertx vertx(VertxProperties properties) {
        return Vertx.vertx(properties.toVertxOptions());
    }
}
