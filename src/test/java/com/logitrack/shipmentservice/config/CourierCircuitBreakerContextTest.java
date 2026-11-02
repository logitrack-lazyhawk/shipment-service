package com.logitrack.shipmentservice.config;

import io.micrometer.context.ContextRegistry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CourierCircuitBreakerContextTest {
    @Test
    void propagatesAndCleansContextAcrossTheCircuitBreakerExecutor() throws Exception {
        ThreadLocal<String> context = new ThreadLocal<>();
        String key = getClass().getName();
        var registry = ContextRegistry.getInstance();
        registry.registerThreadLocalAccessor(key, context);
        var executor = new CourierCircuitBreakerConfig().courierCircuitBreakerExecutor();
        try {
            context.set("request-trace");
            assertEquals("request-trace", executor.submit(context::get).get(5, TimeUnit.SECONDS));
            context.remove();
            assertNull(executor.submit(context::get).get(5, TimeUnit.SECONDS));
        } finally {
            context.remove();
            registry.removeThreadLocalAccessor(key);
            executor.shutdownNow();
        }
    }
}
