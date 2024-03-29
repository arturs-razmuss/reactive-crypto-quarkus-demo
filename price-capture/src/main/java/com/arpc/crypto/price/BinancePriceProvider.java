package com.arpc.crypto.price;

import com.arpc.crypto.price.entity.OrderBookUpdate;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class BinancePriceProvider {
    final Logger logger = Logger.getLogger(BinancePriceProvider.class);

    @Inject
    OrderBookPublisher orderBookChannel;
    @Inject
    ObjectMapper objectMapper;

    @Inject
    MeterRegistry registry;
    @ConfigProperty(name = "arpc.binance.ticker-duration-seconds", defaultValue = "60")
    long tickerDurationSeconds;

    final AtomicBoolean isProcessing = new AtomicBoolean(false);
    WebSocketStreamClientImpl webSocketStreamClient;

    Counter receivedCounter;

    void onStart(@Observes StartupEvent ev) {
        receivedCounter = registry.counter("orderupdate.received", "source", "binance", "symbol", "BTCUSDT");

        isProcessing.set(true);
        webSocketStreamClient = new WebSocketStreamClientImpl();

        var connectionId = webSocketStreamClient.bookTicker("BTCUSDT", this::parseAndSend);

        logger.info("Binance capture starting");
        shutdownSocketWithDelay(connectionId, Duration.ofSeconds(tickerDurationSeconds));
    }

    private void shutdownSocketWithDelay(int connectionId, Duration delay) {
        Uni.createFrom().item("xxx")
                .onItem().delayIt().by(delay)
                .subscribeAsCompletionStage().thenAccept(it -> {
                    System.out.println("shutting down");
                    webSocketStreamClient.closeConnection(connectionId);
                    isProcessing.set(false);
                });
    }

    void parseAndSend(String bookTicker) {
        receivedCounter.increment();
        try {
            var rootNode = objectMapper.readTree(bookTicker);
            var orderBookUpdateRequest = OrderBookUpdate.newBuilder()
                    .setSymbol(rootNode.get("s").asText())
                    .setUpdateId(rootNode.get("u").asLong())
                    .setBestBidPrice(rootNode.get("b").asDouble())
                    .setBestBidQty(rootNode.get("B").asDouble())
                    .setBestAskPrice(rootNode.get("a").asDouble())
                    .setBestAskQty(rootNode.get("A").asDouble())
                    .build();
            orderBookChannel.send(orderBookUpdateRequest);
        } catch (Exception e) {
            logger.error("Sending OrderBookUpdate failed", e);
        }
    }

    void onShutdown(@Observes ShutdownEvent ev) {
        System.out.println("Shutdown signal received. Disconnecting from Binance WebSocket");
        webSocketStreamClient.closeAllConnections();
    }

    public boolean isProcessing() {
        return isProcessing.get();
    }
}
