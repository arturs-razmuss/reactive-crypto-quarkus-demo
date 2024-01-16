package com.arpc.crypto.order.boundary;

import com.arpc.crypto.order.OrderBookQueryService;
import com.arpc.crypto.order.OrderSpreadRequest;
import com.arpc.crypto.order.OrderSpreadResponse;
import com.arpc.crypto.order.entity.Order;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.function.Supplier;

import static com.arpc.util.grpc.converters.TimestampConverter.convertToTimestamp;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@RunOnVertxContext
class OrderGrpcServiceTest {

    @GrpcClient
    OrderBookQueryService orderBookQueryService;

    @Inject
    Mutiny.SessionFactory sessionFactory;
    Uni<?> setupComplete;

    @BeforeEach
    void setUp() {
        Order order = new Order();
        order.symbol = "BTCUSDT";
        order.location = "Binance";
        order.timestamp = Instant.now();
        order.bidPrice = 6;
        order.askPrice = 5;
        setupComplete = sessionFactory.withTransaction(session -> {
            return Order.deleteAll()
                    .onItem().ignore().andSwitchTo(order.persist());
        });
    }

    @Test
    void getBestPrices(UniAsserter asserter) {
        Supplier<Uni<OrderSpreadResponse>> testResultUni = () -> setupComplete.log().onItem().ignore().andSwitchTo(() -> {
                    OrderSpreadRequest request = OrderSpreadRequest.newBuilder()
                            .setSymbol("BTCUSDT")
                            .setStartTimestamp(convertToTimestamp(Instant.now().minusSeconds(60)))
                            .setEndTimestamp(convertToTimestamp(Instant.now().plusSeconds(60)))
                            .build();

                    return orderBookQueryService.getBestPrices(request);
                }
        );
        asserter.assertThat(testResultUni, result -> {
            assertEquals("BTCUSDT", result.getSymbol());
            assertEquals(5, result.getBestAskPrice());
            assertEquals(6, result.getBestBidPrice());
        });
    }
}