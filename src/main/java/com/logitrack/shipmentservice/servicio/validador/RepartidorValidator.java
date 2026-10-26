package com.logitrack.shipmentservice.servicio.validador;

import com.logitrack.shipmentservice.cliente.courier.CourierClient;
import com.logitrack.shipmentservice.cliente.courier.dto.RepartidorClientResponseDto;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Validador de existencia y disponibilidad de repartidores mediante comunicación remota con courier-service.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RepartidorValidator {

    private final CourierClient courierClient;

    /**
     * Valida que un repartidor exista y esté disponible consultando al microservicio courier-service.
     *
     * @param id Identificador UUID del repartidor a verificar.
     * @return DTO con los datos del repartidor remoto si está disponible.
     * @throws IllegalArgumentException si el repartidor no existe (404 Not Found).
     * @throws ReglaNegocioException si el repartidor está inactivo o no disponible.
     */
    public RepartidorClientResponseDto validarDisponibilidadRemota(UUID id) {
        log.debug("Validando disponibilidad remota del repartidor con ID: '{}'", id);

        try {
            RepartidorClientResponseDto dto = courierClient.consultarDisponibilidad(id);

            if (dto == null) {
                log.warn("Respuesta nula recibida desde courier-service para el ID: '{}'", id);
                throw new IllegalArgumentException("Error: No se encontró un repartidor con el ID especificado.");
            }

            if ("INACTIVO".equalsIgnoreCase(dto.estado())) {
                log.warn("El repartidor remoto con ID '{}' se encuentra en estado INACTIVO.", id);
                throw new ReglaNegocioException("Operación no permitida: El repartidor debe estar estrictamente en estado ACTIVO.");
            }

            return dto;
        } catch (FeignException.NotFound e) {
            log.warn("Repartidor no encontrado en courier-service con ID: '{}'", id);
            throw new IllegalArgumentException("Error: No se encontró un repartidor con el ID especificado.");
        } catch (FeignException.BadRequest e) {
            log.warn("El repartidor con ID '{}' no se encuentra disponible en courier-service: {}", id, e.getMessage());
            throw new ReglaNegocioException("Operación no permitida: El repartidor se encuentra inactivo y no está disponible.");
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof ReglaNegocioException) {
                throw e;
            }
            log.error("Fallo de comunicación con courier-service al consultar repartidor '{}': {}", id, e.getMessage());
            throw new ReglaNegocioException("Error de comunicación: No fue posible validar la disponibilidad con courier-service.");
        }
    }
}
