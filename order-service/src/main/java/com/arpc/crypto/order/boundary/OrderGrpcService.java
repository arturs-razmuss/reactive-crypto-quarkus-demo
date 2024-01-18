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

import java.util.Optional;

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
        return Order.findMaxSpreadBetween(request.getSymbol(), convertToInstant(request.getStartTimestamp()), convertToInstant(request.getEndTimestamp()))
                .map(orderDto -> {
                    if (orderDto == null) {
                        throw new StatusRuntimeException(Status.NOT_FOUND.withDescription("No orders found"));
                    }
                    return OrderSpreadResponse.newBuilder()
                            .setSymbol(Optional.of(orderDto.symbol()).orElse("N/A"))
                            .setBestAskPrice(Optional.of(orderDto.maxAskPrice()).orElse(0.0))
                            .setBestBidPrice(Optional.of(orderDto.minBidPrice()).orElse(0.0))
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