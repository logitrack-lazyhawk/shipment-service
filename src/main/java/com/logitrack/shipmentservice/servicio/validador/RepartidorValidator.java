package com.logitrack.shipmentservice.servicio.validador;

import com.logitrack.shipmentservice.dominio.entidad.Repartidor;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.repositorio.RepartidorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Validador de reglas de negocio y comprobación de existencia para repartidores.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RepartidorValidator {

    private final RepartidorRepository repartidorRepository;

    /**
     * Valida que un repartidor exista por su ID y lo retorna.
     * Si no existe, emite un aviso en los logs y lanza IllegalArgumentException (404 Not Found).
     *
     * @param id Identificador UUID del repartidor.
     * @return La entidad Repartidor si fue encontrada.
     * @throws IllegalArgumentException si no se encuentra el repartidor.
     */
    public Repartidor validarYObtenerPorId(UUID id) {
        log.debug("Validando existencia del repartidor con ID: '{}'", id);

        return repartidorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("No se encontró ningún repartidor con el ID: '{}'", id);
                    return new IllegalArgumentException("Error: No se encontró un repartidor con el ID especificado.");
                });
    }

    /**
     * Valida que el repartidor esté activo para poder realizar operaciones operativas (ej. ser asignado a un viaje).
     *
     * @param repartidor Entidad del repartidor a verificar.
     * @throws ReglaNegocioException si el repartidor está inactivo.
     */
    public void validarActivoParaOperacion(Repartidor repartidor) {
        if (repartidor == null) {
            throw new IllegalArgumentException("El repartidor no puede ser nulo.");
        }

        if (repartidor.getEstado() != EstadoRepartidor.ACTIVO) {
            log.warn("Intento de operación con repartidor inactivo. ID: '{}', Estado: '{}'",
                    repartidor.getId(), repartidor.getEstado());
            throw new ReglaNegocioException(
                    "Operación no permitida: El repartidor debe estar estrictamente en estado ACTIVO. Estado actual: " + repartidor.getEstado()
            );
        }
    }
}
