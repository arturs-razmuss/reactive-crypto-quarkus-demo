package com.arpc.crypto.order.boundary;

import com.arpc.crypto.price.entity.OrderBookUpdate;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OrderBookConsumer {
    final Logger logger = LoggerFactory.getLogger(OrderBookConsumer.class);

    @Incoming("orders")
    public void receive(OrderBookUpdate orderBookUpdate) {
        logger.info("Received crypto: {} {}", orderBookUpdate.getBestAskPrice(), orderBookUpdate.getBestBidPrice());
    }
}
