package com.arpc.crypto.order.entity;

import io.quarkus.hibernate.reactive.panache.Panache;
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

    public static Uni<List<Order>> findBetween(String symbol, Instant start, Instant end) {
        return find("symbol = ?1 and timestamp between ?2 and ?3", symbol, start, end).list();
    }

    public static Uni<OrderAmountDto> findMaxSpreadBetween(String symbol, Instant start, Instant end) {
        return Panache.getSession().flatMap(session -> {
            var query = session.createQuery("""
                    SELECT new com.arpc.crypto.order.entity.OrderAmountDto(symbol, e.askPrice, e.bidPrice)
                    FROM orders e
                    WHERE symbol = :symbol and timestamp between :start and :end
                    ORDER BY e.askPrice - e.bidPrice DESC
                    """, OrderAmountDto.class);
            query.setParameter("symbol", symbol);
            query.setParameter("start", start);
            query.setParameter("end", end);
            query.setMaxResults(1);
            query.setReadOnly(true);
            return query.getSingleResultOrNull();
        });
    }

}
