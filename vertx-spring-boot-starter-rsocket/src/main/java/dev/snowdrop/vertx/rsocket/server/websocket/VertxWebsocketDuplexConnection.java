package dev.snowdrop.vertx.rsocket.server.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.internal.BaseDuplexConnection;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocketBase;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

public class VertxWebsocketDuplexConnection extends BaseDuplexConnection {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final WebSocketBase webSocketBase;

    private final AtomicReference<Flux<ByteBuf>> fluxReference = new AtomicReference<>();

    public VertxWebsocketDuplexConnection(WebSocketBase webSocketBase) {
        this.webSocketBase = webSocketBase;
    }

    @Override
    protected void doOnClose() {
        if (!webSocketBase.isClosed()) {
            webSocketBase.close();
        }
    }

    @NotNull
    @Override
    public Mono<Void> send(@NotNull Publisher<ByteBuf> frames) {
        if (frames instanceof Mono) {
            return ((Mono<ByteBuf>) frames)
                .map(byteBuf -> {
                    Buffer buffer = Buffer.buffer(byteBuf);
                    webSocketBase.writeFinalBinaryFrame(buffer);
                    return buffer;
                })
                .then();
        } else {
            return ((Flux<ByteBuf>) frames)
                .map(byteBuf -> {
                    Buffer buffer = Buffer.buffer(byteBuf);
                    webSocketBase.writeFinalBinaryFrame(buffer);
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
            flux = Flux.create(sink -> webSocketBase.frameHandler(
                frame -> {
                    if (frame.isClose()) {
                        sink.complete();
                        return;
                    }
                    Buffer buffer = frame.binaryData();
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
