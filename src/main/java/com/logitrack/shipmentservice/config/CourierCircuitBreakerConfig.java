package com.logitrack.shipmentservice.config;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration(proxyBeanMethods = false)
public class CourierCircuitBreakerConfig {
    // TimeLimiter runs Feign on another thread. Preserve its trace and observation context.
    @Bean(destroyMethod = "shutdown")
    ExecutorService courierCircuitBreakerExecutor() {
        return ContextExecutorService.wrap(Executors.newCachedThreadPool(),
                ContextSnapshotFactory.builder().build()::captureAll);
    }

    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> courierCircuitBreakerCustomizer(
            ExecutorService courierCircuitBreakerExecutor) {
        return factory -> factory.configureExecutorService(courierCircuitBreakerExecutor);
    }
}
