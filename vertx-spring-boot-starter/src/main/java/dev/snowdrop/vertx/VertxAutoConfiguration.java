package dev.snowdrop.vertx;

import dev.snowdrop.vertx.reactor.VertxScheduler;
import io.vertx.core.Vertx;
import io.vertx.core.spi.cluster.ClusterManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.core.scheduler.Schedulers.Factory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

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
        Vertx vertx = future.get();
        updateReactorSchedulers(vertx);
        return vertx;
    }

    // Let the Vertx user to handle instance closing.
    // This is done in particular for HTTP server which is closed by Spring Context after beans are destroyed.
    // Allowing Vertx bean to be destroyed by the context would block HTTP server from calling its close method.
    @ConditionalOnMissingBean({ Vertx.class, ClusterManager.class })
    @Bean(destroyMethod = "")
    public Vertx vertx(VertxProperties properties) {
        Vertx vertx = Vertx.vertx(properties.toVertxOptions());
        updateReactorSchedulers(vertx);
        return vertx;
    }

    private void updateReactorSchedulers(Vertx vertx) {
        Schedulers.setFactory(new Factory() {
            @Override
            public Scheduler newElastic(int ttlSeconds, ThreadFactory threadFactory) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Scheduler newBoundedElastic(int threadCap, int queuedTaskCap, ThreadFactory threadFactory, int ttlSeconds) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Scheduler newParallel(int parallelism, ThreadFactory threadFactory) {
                return new VertxScheduler(vertx);
            }

            @Override
            public Scheduler newSingle(ThreadFactory threadFactory) {
                return new VertxScheduler(vertx);
            }
        });
    }
}
