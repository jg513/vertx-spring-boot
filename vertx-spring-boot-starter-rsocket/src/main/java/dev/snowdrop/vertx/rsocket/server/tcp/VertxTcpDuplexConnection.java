package dev.snowdrop.vertx.rsocket.server.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.internal.BaseDuplexConnection;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

public class VertxTcpDuplexConnection extends BaseDuplexConnection {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NetSocket netSocket;

    private final AtomicReference<Flux<ByteBuf>> fluxReference = new AtomicReference<>();

    private boolean closed;

    public VertxTcpDuplexConnection(NetSocket netSocket) {
        this.netSocket = netSocket;
    }

    @Override
    protected void doOnClose() {
        if (!closed) {
            this.closed = true;
            this.netSocket.close();
        }
    }

    @NotNull
    @Override
    public Mono<Void> send(@NotNull Publisher<ByteBuf> frames) {
        if (frames instanceof Mono) {
            return ((Mono<ByteBuf>) frames)
                .map(byteBuf -> {
                    Buffer buffer = Buffer.buffer(byteBuf);
                    netSocket.write(buffer);
                    return buffer;
                })
                .then();
        } else {
            return ((Flux<ByteBuf>) frames)
                .map(byteBuf -> {
                    Buffer buffer = Buffer.buffer(byteBuf);
                    netSocket.write(buffer);
                    return buffer;
                })
                .then();
        }
    }

    @NotNull
    @Override
    public Flux<ByteBuf> receive() {
        Flux<ByteBuf> flux = fluxReference.get();
        if (flux == null) {
            flux = Flux.create(sink -> netSocket.handler(
                buffer -> {
                    ByteBuf byteBuf = buffer.getByteBuf();
                    sink.next(byteBuf);
                }
            ));
            if (!fluxReference.compareAndSet(null, flux)) {
                return fluxReference.get();
            }
        }
        logger.info("Create receiving flux {}", flux.hashCode());
        return flux;
    }

    @NotNull
    @Override
    public ByteBufAllocator alloc() {
        return ByteBufAllocator.DEFAULT;
    }
}
