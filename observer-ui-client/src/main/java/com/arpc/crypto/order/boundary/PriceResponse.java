package com.arpc.crypto.order.boundary;

public record PriceResponse(String symbol, double bestBidPrice, double bestAskPrice) {
}
