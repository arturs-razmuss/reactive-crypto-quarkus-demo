package com.arpc.crypto.order.boundary;

import com.arpc.crypto.order.entity.Order;
import com.arpc.crypto.price.entity.OrderBookUpdate;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@ApplicationScoped
public class OrderBookConsumer {
    final Logger logger = LoggerFactory.getLogger(OrderBookConsumer.class);

    @Incoming("orders")
    @WithSession
    @WithTransaction
    public Uni<?> receive(ConsumerRecord<Long,OrderBookUpdate> orderUpdateRecord) {
        Order orderToSave = convertToOrder(orderUpdateRecord);

        return orderToSave.persist()
                .onItem().invoke((savedOrder) -> {
            var order = (Order)savedOrder;
            logger.info("Saving crypto: {} {}", order.askPrice, order.bidPrice);
        });
    }

    @NotNull
    private static Order convertToOrder(ConsumerRecord<Long, OrderBookUpdate> orderUpdateRecord) {
        var orderBookUpdate = orderUpdateRecord.value();
        Order order = new Order();
        order.symbol = orderBookUpdate.getSymbol();
        order.askPrice = orderBookUpdate.getBestAskPrice();
        order.bidPrice = orderBookUpdate.getBestBidPrice();
        order.timestamp = Instant.ofEpochMilli(orderUpdateRecord.timestamp());
        return order;
    }
}
