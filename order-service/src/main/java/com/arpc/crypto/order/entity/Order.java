package com.arpc.crypto.order.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Entity;

import java.time.Instant;
import java.util.List;

@Entity(name = "orders")
public class Order extends PanacheEntity {

    public String symbol;
    public String location;
    public Instant timestamp;
    public double bidPrice;
    public double askPrice;

    public static Uni<List<Order>> findBetween(Instant start, Instant end) {
        return find("timestamp between ?1 and ?2", start, end).list();
    }

}
