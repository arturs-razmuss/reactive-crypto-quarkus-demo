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
import java.util.stream.Stream;

@QuarkusTest
@RunOnVertxContext
class OrderTest {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    long currentEpochMillis;
    Order initialOrder;
    @BeforeEach
    void setUp() {
        sessionFactory.withSession(session -> {
            Order.deleteAll().await().atMost(Duration.ofSeconds(5));
            Order order = new Order();
            order.symbol = "BTCUSDT";
            order.location = "Binance";
            currentEpochMillis = System.currentTimeMillis();
            order.timestamp = Instant.ofEpochMilli(currentEpochMillis);
            order.bidPrice = 10000;
            order.askPrice = 10001;

            order.persist().await().atMost(Duration.ofSeconds(5));
            return Uni.createFrom().nullItem();
        });
    }

    @Test
    void shouldFindIfTimestampIsInRange(TransactionalUniAsserter asserter) {
        asserter.assertEquals(() -> Order.findBetween(Instant.ofEpochMilli(currentEpochMillis), Instant.ofEpochMilli(currentEpochMillis))
                .map(List::stream)
                .map(Stream::findFirst)
                .map(Optional::orElseThrow), initialOrder);
    }

    @Test
    void shouldFindWhenPeriodBeforeTimestamp(TransactionalUniAsserter asserter) {
        asserter.assertEquals(() -> Order.findBetween(Instant.ofEpochMilli(currentEpochMillis - 1000), Instant.ofEpochMilli(currentEpochMillis-1))
                .map(List::stream)
                .map(Stream::findFirst)
                .map(Optional::orElseThrow), initialOrder);
    }

    @Test
    void shouldExcludeWhenPeriodAfterTimestamp(TransactionalUniAsserter asserter) {
        asserter.assertEquals(() -> Order.findBetween(Instant.ofEpochMilli(currentEpochMillis+1), Instant.ofEpochMilli(currentEpochMillis + 1000))
                .map(List::stream)
                .map(Stream::findFirst)
                .map(Optional::orElseThrow), initialOrder);
    }
}