package com.logitrack.shipmentservice.config;

import com.logitrack.shipmentservice.cliente.courier.CourierClient;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class CourierClientFallback implements FallbackFactory<CourierClient> {
    @Override
    public CourierClient create(Throwable cause) {
        return id -> {
            if (cause instanceof IllegalArgumentException error) {
                throw error;
            }
            if (cause instanceof ReglaNegocioException error) {
                throw error;
            }
            if (cause instanceof FeignException.NotFound error) {
                throw error;
            }
            if (cause instanceof FeignException.BadRequest error) {
                throw error;
            }
            throw new IllegalStateException(
                    "Servicio no disponible: No se pudo conectar con courier-service o el tiempo de espera se agotó.", cause);
        };
    }
}
