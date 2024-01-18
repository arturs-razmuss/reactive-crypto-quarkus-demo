package com.arpc.crypto.order.entity;

public record OrderAmountDto(String symbol, double maxAskPrice, double minBidPrice) {
}