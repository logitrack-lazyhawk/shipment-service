package com.logitrack.shipmentservice.config;

import com.logitrack.shipmentservice.cliente.courier.CourierClient;
import com.logitrack.shipmentservice.dominio.exception.CourierServiceUnavailableException;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoDisponibleException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoEncontradoException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * Fábrica de contingencia (FallbackFactory) para el cliente Feign de courier-service.
 * Garantiza que ante caídas de red, timeouts o apertura del Circuit Breaker,
 * se preserve la semántica de errores de negocio y se devuelvan excepciones tipadas coherentes.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 2.0.0
 */
@Slf4j
@Component
public class CourierClientFallback implements FallbackFactory<CourierClient> {

    @Override
    public CourierClient create(Throwable cause) {
        return id -> {
            log.warn("Ejecutando contingencia (Fallback) para consulta de repartidor '{}'. Causa: {}",
                    id, cause != null ? cause.toString() : "desconocida");

            if (cause instanceof RepartidorNoEncontradoException notFound) {
                throw notFound;
            }
            if (cause instanceof RepartidorNoDisponibleException notAvailable) {
                throw notAvailable;
            }
            if (cause instanceof ReglaNegocioException businessError) {
                throw businessError;
            }
            if (cause instanceof IllegalArgumentException illegalArg) {
                throw illegalArg;
            }
            if (cause instanceof FeignException.NotFound) {
                throw new RepartidorNoEncontradoException(
                        "Error: No se encontró un repartidor con el ID especificado en courier-service.", cause);
            }
            if (cause instanceof FeignException.BadRequest) {
                throw new RepartidorNoDisponibleException(
                        "Operación no permitida: El repartidor se encuentra inactivo y no está disponible.");
            }
            if (cause instanceof CourierServiceUnavailableException unavailable) {
                throw unavailable;
            }

            throw new CourierServiceUnavailableException(
                    "Servicio no disponible: No se pudo conectar con courier-service o el tiempo de espera se agotó.", cause);
        };
    }
}
