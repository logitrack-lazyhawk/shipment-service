package com.logitrack.shipmentservice.config;

import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoDisponibleException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoEncontradoException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshotFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configuración de tolerancia a fallos, Circuit Breaker y Timeouts para courier-service.
 * Define la ventana deslizante, tasa de fallos, tiempos de apertura y propagación de contexto
 * para observabilidad y trazabilidad distribuida (Zipkin / Micrometer).
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 2.0.0
 */
@Configuration(proxyBeanMethods = false)
public class CourierCircuitBreakerConfig {

    // TimeLimiter ejecuta Feign en otro hilo. Preservar contexto de observación y trazas.
    @Bean(destroyMethod = "shutdown")
    ExecutorService courierCircuitBreakerExecutor() {
        return ContextExecutorService.wrap(Executors.newCachedThreadPool(),
                ContextSnapshotFactory.builder().build()::captureAll);
    }

    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> courierCircuitBreakerCustomizer(
            ExecutorService courierCircuitBreakerExecutor) {
        return factory -> {
            factory.configureExecutorService(courierCircuitBreakerExecutor);
            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                            .timeoutDuration(Duration.ofSeconds(3))
                            .cancelRunningFuture(true)
                            .build())
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                            .slidingWindowSize(5)
                            .minimumNumberOfCalls(3)
                            .failureRateThreshold(50.0f)
                            .waitDurationInOpenState(Duration.ofSeconds(5))
                            .permittedNumberOfCallsInHalfOpenState(2)
                            .slowCallRateThreshold(50.0f)
                            .slowCallDurationThreshold(Duration.ofSeconds(2))
                            .ignoreExceptions(
                                    RepartidorNoEncontradoException.class,
                                    RepartidorNoDisponibleException.class,
                                    ReglaNegocioException.class,
                                    IllegalArgumentException.class
                            )
                            .build())
                    .build());
        };
    }
}
