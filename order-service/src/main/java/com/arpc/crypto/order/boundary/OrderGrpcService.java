package com.arpc.crypto.order.boundary;

import com.arpc.crypto.order.OrderBookQueryService;
import com.arpc.crypto.order.OrderSpreadRequest;
import com.arpc.crypto.order.OrderSpreadResponse;
import com.arpc.crypto.order.SymbolRequest;
import com.arpc.crypto.order.entity.Order;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcService;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.reactive.messaging.Channel;

import static com.arpc.util.grpc.converters.TimestampConverter.convertToInstant;

@GrpcService
@Singleton
public class OrderGrpcService implements OrderBookQueryService {

    @Inject
    @Channel("prices-in-memory")
    Multi<Order> incomingOrders;

    @WithSession
    @Override
    public Uni<OrderSpreadResponse> getBestPrices(OrderSpreadRequest request) {
        return Order.findBetween(request.getSymbol(), convertToInstant(request.getStartTimestamp()), convertToInstant(request.getEndTimestamp()))
                .map(orders -> {
                    if (orders.isEmpty()) {
                        throw new StatusRuntimeException(Status.NOT_FOUND.withDescription("No orders found"));
                    }
                    return OrderSpreadResponse.newBuilder()
                            .setSymbol(orders.stream().map(order -> order.symbol).findAny().orElse(""))
                            .setBestAskPrice(orders.stream().mapToDouble(order -> order.askPrice).max().orElse(0))
                            .setBestBidPrice(orders.stream().mapToDouble(order -> order.bidPrice).min().orElse(0))
                            .build();
                });
    }

    @Override
    public Multi<OrderSpreadResponse> getPriceStream(SymbolRequest request) {
        return incomingOrders.filter(order -> order.symbol.equals(request.getSymbol()))
                .map(order -> OrderSpreadResponse.newBuilder()
                        .setSymbol(order.symbol)
                        .setBestAskPrice(order.askPrice)
                        .setBestBidPrice(order.bidPrice)
                        .build());
    }
}