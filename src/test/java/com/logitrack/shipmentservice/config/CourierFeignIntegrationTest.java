package com.logitrack.shipmentservice.config;

import com.logitrack.shipmentservice.cliente.courier.CourierClient;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import feign.Client;
import feign.Response;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises the real Feign proxy, decoder, circuit breaker and fallback without network I/O. */
@SpringBootTest(classes = CourierFeignIntegrationTest.TestApplication.class, properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=true",
        "spring.cloud.openfeign.client.config.courier-service.url=http://courier-service",
        "spring.jpa.hibernate.ddl-auto=none",
        "resilience4j.circuitbreaker.configs.default.ignoreExceptions[0]=java.lang.IllegalArgumentException",
        "resilience4j.circuitbreaker.configs.default.ignoreExceptions[1]=feign.FeignException$BadRequest",
        "management.tracing.export.zipkin.enabled=false"
})
class CourierFeignIntegrationTest {
    @Autowired CourierClient client;
    @Autowired AtomicInteger upstreamStatus;
    @Autowired CircuitBreakerRegistry registry;

    @BeforeEach
    void resetCircuits() {
        registry.getAllCircuitBreakers().forEach(breaker -> breaker.reset());
    }

    @Test
    void successfulResponseIsDecodedByFeign() {
        upstreamStatus.set(200);
        assertEquals("ACTIVO", client.consultarDisponibilidad(UUID.randomUUID()).estado());
    }

    @Test
    void businessErrorsDoNotCountAsCircuitFailures() {
        for (int status : new int[]{404, 400}) {
            upstreamStatus.set(status);
            for (int attempt = 0; attempt < 6; attempt++) {
                assertThrows(RuntimeException.class, () -> client.consultarDisponibilidad(UUID.randomUUID()));
            }
        }
        registry.getAllCircuitBreakers().forEach(breaker -> {
            assertEquals(0, breaker.getMetrics().getNumberOfFailedCalls());
            assertEquals(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.CLOSED, breaker.getState());
        });
    }

    @Test
    void missingCourierRemainsNotFoundThroughTheRealFallback() {
        upstreamStatus.set(404);
        assertThrows(IllegalArgumentException.class, () -> client.consultarDisponibilidad(UUID.randomUUID()));
    }

    @Test
    void unavailableCourierUsesFallbackForServerErrorsAndConnectionFailures() {
        for (int status : new int[]{500, -1}) {
            upstreamStatus.set(status);
            var error = assertThrows(IllegalStateException.class,
                    () -> client.consultarDisponibilidad(UUID.randomUUID()));
            assertTrue(error.getMessage().startsWith("Servicio no disponible"));
        }
    }

    @Test
    void inactiveCourierRemainsABusinessError() {
        upstreamStatus.set(400);
        var validator = new com.logitrack.shipmentservice.servicio.validador.RepartidorValidator(client);
        assertThrows(ReglaNegocioException.class, () -> validator.validarDisponibilidadRemota(UUID.randomUUID()));
    }

    @Test
    void openCircuitUsesFallbackWithoutCallingCourier() {
        upstreamStatus.set(500);
        assertThrows(IllegalStateException.class, () -> client.consultarDisponibilidad(UUID.randomUUID()));
        assertFalse(registry.getAllCircuitBreakers().isEmpty());
        registry.getAllCircuitBreakers().forEach(breaker -> breaker.transitionToOpenState());
        upstreamStatus.set(200);
        assertThrows(IllegalStateException.class, () -> client.consultarDisponibilidad(UUID.randomUUID()));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableFeignClients(clients = CourierClient.class)
    @Import({CourierClientFallback.class, CourierCircuitBreakerConfig.class})
    static class TestApplication {
        @Bean AtomicInteger upstreamStatus() { return new AtomicInteger(200); }

        @Bean
        Client courierTransport(AtomicInteger upstreamStatus) {
            return (request, options) -> {
                int status = upstreamStatus.get();
                if (status == -1) throw new IOException("Simulated connection failure");
                return Response.builder().request(request).status(status)
                        .headers(Map.of("Content-Type", java.util.List.of("application/json")))
                        .body("{\"id\":\"00000000-0000-0000-0000-000000000001\",\"estado\":\"ACTIVO\"}", StandardCharsets.UTF_8)
                        .reason("Simulated courier response").build();
            };
        }
    }
}
