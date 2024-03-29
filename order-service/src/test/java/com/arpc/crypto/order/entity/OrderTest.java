package com.arpc.crypto.order.entity;

import io.quarkus.test.hibernate.reactive.panache.TransactionalUniAsserter;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class OrderTest {

    public static final String BTCUSDT = "BTCUSDT";
    @Inject
    Mutiny.SessionFactory sessionFactory;

    long currentEpochMillis;
    Order initialOrder;

    @BeforeEach
    void setUp() {
        sessionFactory.withTransaction((session, transaction) -> {
            Order order = new Order();
            order.symbol = BTCUSDT;
            order.location = "Binance";
            currentEpochMillis = System.currentTimeMillis();
            order.timestamp = Instant.ofEpochMilli(currentEpochMillis);
            order.bidPrice = 10000;
            order.askPrice = 10001;
            initialOrder = order;

            return Order.deleteAll()
                    .replaceWith(
                            session.persist(order)
                            .onFailure().invoke(Throwable::printStackTrace));
        }).await().atMost(Duration.ofSeconds(5));
    }

    @Test
    @RunOnVertxContext
    void shouldFindIfTimestampIsInRange(TransactionalUniAsserter asserter) {
        Supplier<Uni<Order>> foundOrder = () -> Order.findBetween(BTCUSDT, Instant.ofEpochMilli(currentEpochMillis), Instant.ofEpochMilli(currentEpochMillis))
                .map(List::stream)
                .map(Stream::findFirst)
                .map(Optional::orElseThrow);

        asserter.assertThat(foundOrder, (entity) -> assertEquals(BTCUSDT, entity.symbol));

    }

    @RunOnVertxContext
    @Test
    void shouldExcludeWhenPeriodBeforeTimestamp(TransactionalUniAsserter asserter) {
        asserter.assertTrue(() -> Order.findBetween(BTCUSDT, Instant.ofEpochMilli(currentEpochMillis - 1000), Instant.ofEpochMilli(currentEpochMillis-1))
                .map(List::stream)
                .map(Stream::findFirst)
                .map(Optional::isEmpty));
    }

    @RunOnVertxContext
    @Test
    void shouldExcludeWhenPeriodAfterTimestamp(TransactionalUniAsserter asserter) {
        asserter.assertTrue(() -> Order.findBetween(BTCUSDT, Instant.ofEpochMilli(currentEpochMillis+1), Instant.ofEpochMilli(currentEpochMillis + 1000))
                .map(List::stream)
                .map(Stream::findFirst)
                .map(Optional::isEmpty));
    }
}