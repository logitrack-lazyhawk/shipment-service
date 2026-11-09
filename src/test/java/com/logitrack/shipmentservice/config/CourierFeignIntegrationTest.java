package com.logitrack.shipmentservice.config;

import com.logitrack.shipmentservice.cliente.courier.CourierClient;
import com.logitrack.shipmentservice.dominio.exception.CourierServiceUnavailableException;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoDisponibleException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoEncontradoException;
import feign.Client;
import feign.Response;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para Feign, ErrorDecoder, CircuitBreaker y Fallback
 * comprobando fallos, recuperación y observabilidad.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 2.0.0
 */
@SpringBootTest(classes = CourierFeignIntegrationTest.TestApplication.class, properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=true",
        "spring.cloud.openfeign.client.config.courier-service.url=http://courier-service",
        "spring.cloud.openfeign.client.config.courier-service.connectTimeout=2000",
        "spring.cloud.openfeign.client.config.courier-service.readTimeout=3000",
        "spring.jpa.hibernate.ddl-auto=none",
        "resilience4j.circuitbreaker.configs.default.ignoreExceptions[0]=java.lang.IllegalArgumentException",
        "resilience4j.circuitbreaker.configs.default.ignoreExceptions[1]=feign.FeignException$BadRequest",
        "resilience4j.circuitbreaker.configs.default.ignoreExceptions[2]=com.logitrack.shipmentservice.dominio.exception.RepartidorNoEncontradoException",
        "resilience4j.circuitbreaker.configs.default.ignoreExceptions[3]=com.logitrack.shipmentservice.dominio.exception.RepartidorNoDisponibleException",
        "resilience4j.circuitbreaker.configs.default.ignoreExceptions[4]=com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException",
        "management.tracing.export.zipkin.enabled=false"
})
class CourierFeignIntegrationTest {

    @Autowired CourierClient client;
    @Autowired AtomicInteger upstreamStatus;
    @Autowired CircuitBreakerRegistry registry;

    @BeforeEach
    void resetCircuits() {
        registry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
    }

    @Test
    @DisplayName("Respuesta exitosa (HTTP 200) es decodificada correctamente por Feign")
    void successfulResponseIsDecodedByFeign() {
        upstreamStatus.set(200);
        assertEquals("ACTIVO", client.consultarDisponibilidad(UUID.randomUUID()).estado());
    }

    @Test
    @DisplayName("Errores de negocio (404, 400) no cuentan como fallos para abrir el Circuit Breaker")
    void businessErrorsDoNotCountAsCircuitFailures() {
        for (int status : new int[]{404, 400}) {
            upstreamStatus.set(status);
            for (int attempt = 0; attempt < 6; attempt++) {
                assertThrows(RuntimeException.class, () -> client.consultarDisponibilidad(UUID.randomUUID()));
            }
        }
        registry.getAllCircuitBreakers().forEach(breaker -> {
            assertEquals(0, breaker.getMetrics().getNumberOfFailedCalls());
            assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());
        });
    }

    @Test
    @DisplayName("Repartidor no encontrado (404) lanza RepartidorNoEncontradoException a través del Fallback")
    void missingCourierRemainsNotFoundThroughTheRealFallback() {
        upstreamStatus.set(404);
        assertThrows(RepartidorNoEncontradoException.class, () -> client.consultarDisponibilidad(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Servicio caído o error 500 activa el Fallback y lanza CourierServiceUnavailableException")
    void unavailableCourierUsesFallbackForServerErrorsAndConnectionFailures() {
        for (int status : new int[]{500, -1}) {
            upstreamStatus.set(status);
            var error = assertThrows(CourierServiceUnavailableException.class,
                    () -> client.consultarDisponibilidad(UUID.randomUUID()));
            assertTrue(error.getMessage().startsWith("Servicio no disponible"));
        }
    }

    @Test
    @DisplayName("Repartidor inactivo (HTTP 400) permanece como error de regla de negocio")
    void inactiveCourierRemainsABusinessError() {
        upstreamStatus.set(400);
        var validator = new com.logitrack.shipmentservice.servicio.validador.RepartidorValidator(client);
        assertThrows(ReglaNegocioException.class, () -> validator.validarDisponibilidadRemota(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Circuito abierto (OPEN) activa Fallback de inmediato sin llamar a courier-service")
    void openCircuitUsesFallbackWithoutCallingCourier() {
        upstreamStatus.set(500);
        assertThrows(CourierServiceUnavailableException.class, () -> client.consultarDisponibilidad(UUID.randomUUID()));
        assertFalse(registry.getAllCircuitBreakers().isEmpty());
        registry.getAllCircuitBreakers().forEach(CircuitBreaker::transitionToOpenState);
        upstreamStatus.set(200);
        assertThrows(CourierServiceUnavailableException.class, () -> client.consultarDisponibilidad(UUID.randomUUID()));
    }

    @Test
    @DisplayName("F16: Recuperación del Circuit Breaker desde OPEN -> HALF_OPEN -> CLOSED")
    void circuitBreakerRecoversFromOpenToHalfOpenToClosed() {
        assertFalse(registry.getAllCircuitBreakers().isEmpty());
        CircuitBreaker breaker = registry.getAllCircuitBreakers().iterator().next();

        // 1. Simular circuito abierto ante fallas reiteradas
        breaker.transitionToOpenState();
        assertEquals(CircuitBreaker.State.OPEN, breaker.getState());

        // 2. Transicionar a estado de prueba (HALF_OPEN)
        breaker.transitionToHalfOpenState();
        assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.getState());

        // 3. Simular que el microservicio remoto se recuperó (HTTP 200)
        upstreamStatus.set(200);
        int permittedCalls = breaker.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState();
        for (int i = 0; i < permittedCalls; i++) {
            assertEquals("ACTIVO", client.consultarDisponibilidad(UUID.randomUUID()).estado());
        }

        // 4. El Circuit Breaker verifica que el servicio está sano y se cierra automáticamente
        assertEquals(CircuitBreaker.State.CLOSED, breaker.getState());
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
