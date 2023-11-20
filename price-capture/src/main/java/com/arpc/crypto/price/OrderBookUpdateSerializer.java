package com.arpc.crypto.price;

import com.arpc.crypto.price.entity.OrderBookUpdate;
import org.apache.kafka.common.serialization.Serializer;

public class OrderBookUpdateSerializer implements Serializer<OrderBookUpdate> {

    @Override
    public byte[] serialize(String topic, OrderBookUpdate data) {
        return data.toByteArray();
    }
}
