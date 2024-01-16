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

@Path("/crypto")
@Produces(MediaType.APPLICATION_JSON)
public class OrderBookResource {

    @GrpcClient("orderBookQueryService")
    OrderBookQueryService orderBookQueryService;

    @GET
    @Path("/{symbol}")
    public Uni<PriceResponse> hello(@PathParam("symbol") String symbol) {
        return orderBookQueryService.getBestPrices(OrderSpreadRequest.newBuilder()
                        .setSymbol(symbol)
                        .setStartTimestamp(TimestampConverter.convertToTimestamp(Instant.now().minusSeconds(3600)))
                        .setEndTimestamp(TimestampConverter.convertToTimestamp(Instant.now()))
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
