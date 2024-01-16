package com.arpc.crypto.order.boundary;

import com.arpc.crypto.order.OrderBookQueryService;
import com.arpc.crypto.order.OrderSpreadRequest;
import com.arpc.crypto.order.SymbolRequest;
import com.arpc.util.grpc.converters.TimestampConverter;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestStreamElementType;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.Instant;
import java.util.Optional;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderBookResource {

    @GrpcClient("orderBookQueryService")
    OrderBookQueryService orderBookQueryService;

    @GET
    @Path("/{symbol}")
    public Uni<PriceResponse> getBestPriceWithinPeriod(@PathParam("symbol") String symbol,
                                                       @QueryParam("start") Instant start,
                                                       @QueryParam("end") Instant end) {
        var startTimestamp = TimestampConverter.convertToTimestamp(Optional.ofNullable(start).orElse(Instant.now().minusSeconds(3600)));
        var endTimestamp = TimestampConverter.convertToTimestamp(Optional.ofNullable(end).orElse(Instant.now()));
        return orderBookQueryService.getBestPrices(OrderSpreadRequest.newBuilder()
                        .setSymbol(symbol)
                        .setStartTimestamp(startTimestamp)
                        .setEndTimestamp(endTimestamp)
                        .build())
                .map(orderSpreadResponse -> new PriceResponse(
                        orderSpreadResponse.getSymbol(),
                        orderSpreadResponse.getBestBidPrice(),
                        orderSpreadResponse.getBestAskPrice())
                );
    }

    @GET
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    @Path("/{symbol}/sse")
    public Multi<PriceResponse> getPriceStream(@PathParam("symbol") String symbol) {
        return orderBookQueryService.getPriceStream(SymbolRequest.newBuilder()
                        .setSymbol(symbol)
                        .build()
                )
                .map(orderSpreadResponse -> new PriceResponse(
                        orderSpreadResponse.getSymbol(),
                        orderSpreadResponse.getBestBidPrice(),
                        orderSpreadResponse.getBestAskPrice())
                );
    }

    @ServerExceptionMapper
    public Uni<RestResponse<?>> mapException(StatusRuntimeException grpcException) {
        var httpStatus = switch (grpcException.getStatus().getCode()) {
            case INVALID_ARGUMENT -> Response.Status.BAD_REQUEST;
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };

        return Uni.createFrom().item(RestResponse.status(httpStatus));
    }

}
