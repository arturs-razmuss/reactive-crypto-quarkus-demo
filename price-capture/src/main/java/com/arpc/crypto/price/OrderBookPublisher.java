package com.arpc.crypto.price;

import com.arpc.crypto.price.entity.OrderBookUpdate;
import io.micrometer.core.annotation.Counted;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class OrderBookPublisher {

    @Inject
    @Channel("raw-price")
    Emitter<Record<Long, OrderBookUpdate>> emitter;

    @Counted(value = "orderupdate.sent", description = "How many order book updates are published")
    public void send(OrderBookUpdate orderBookUpdate) {
        emitter.send(Record.of(orderBookUpdate.getUpdateId(), orderBookUpdate));
    }
}
