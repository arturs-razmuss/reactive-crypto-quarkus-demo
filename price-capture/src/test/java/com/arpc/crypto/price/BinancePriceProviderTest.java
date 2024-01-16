package com.arpc.crypto.price;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@QuarkusTest
class BinancePriceProviderTest {

    @Inject
    BinancePriceProvider binancePriceProvider;

    @Test
    void onStart() {
        await().atMost(3, TimeUnit.SECONDS).until(() -> binancePriceProvider.isProcessing());
        await().atMost(3, TimeUnit.SECONDS).until(() -> !binancePriceProvider.isProcessing());
    }
}