package com.arpc.crypto.order.boundary;

import java.time.Instant;

public class OrderDto {
    public String symbol;
    public String location;
    public Instant timestamp;
    public double bidPrice;
    public double askPrice;
}
