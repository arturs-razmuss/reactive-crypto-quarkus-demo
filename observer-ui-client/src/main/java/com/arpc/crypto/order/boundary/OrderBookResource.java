package com.arpc.crypto.order.boundary;

import com.arpc.crypto.order.OrderBookQueryService;
import com.arpc.crypto.order.OrderSpreadRequest;
import com.arpc.util.grpc.converters.TimestampConverter;
import io.quarkus.grpc.GrpcClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;

@Path("/hello")
public class OrderBookResource {

    @GrpcClient("orderBookQueryService")
    OrderBookQueryService orderBookQueryService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        orderBookQueryService.getBestPrices(OrderSpreadRequest.newBuilder()
                .setSymbol("BTC")
                .setStartTimestamp(TimestampConverter.convertToTimestamp(Instant.now().minusSeconds(60)))
                .setEndTimestamp(TimestampConverter.convertToTimestamp(Instant.now()))
                .build())
                .subscribe().with(orderSpreadResponse -> {
            System.out.println("Best ask price: " + orderSpreadResponse.getBestAskPrice());
            System.out.println("Best bid price: " + orderSpreadResponse.getBestBidPrice());
        }, throwable -> {
            System.out.println("Error: " + throwable.getMessage());
        });
        return "Hello from RESTEasy Reactive";
    }
}
