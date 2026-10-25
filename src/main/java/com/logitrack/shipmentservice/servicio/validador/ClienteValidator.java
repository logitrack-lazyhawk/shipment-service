package com.logitrack.shipmentservice.servicio.validador;

import com.logitrack.shipmentservice.dominio.dto.ClienteRequestDto;
import com.logitrack.shipmentservice.dominio.entidad.Cliente;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.repositorio.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Validador de reglas de negocio y comprobación de existencia para clientes.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteValidator {

    private final ClienteRepository clienteRepository;

    /**
     * Valida que un cliente exista por su ID y lo retorna.
     * Si no existe, emite un aviso en los logs y lanza IllegalArgumentException (404 Not Found).
     *
     * @param id Identificador UUID del cliente.
     * @return La entidad Cliente si fue encontrada.
     * @throws IllegalArgumentException si no se encuentra el cliente.
     */
    public Cliente validarYObtenerPorId(UUID id) {
        log.debug("Validando existencia del cliente con ID: '{}'", id);

        return clienteRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("No se encontró ningún cliente con el ID: '{}'", id);
                    return new IllegalArgumentException("Error: No se encontró un cliente con el ID especificado.");
                });
    }

    /**
     * Valida que los datos únicos del cliente (DNI y Email) no estén duplicados al registrar.
     *
     * @param peticion Datos del cliente a validar.
     * @throws ReglaNegocioException si el DNI o Email ya se encuentran en uso.
     */
    public void validarParaCreacion(ClienteRequestDto peticion) {
        if (peticion == null) {
            throw new IllegalArgumentException("Los datos del cliente no pueden ser nulos.");
        }

        if (clienteRepository.existsByDni(peticion.dni())) {
            log.warn("Intento de registro con DNI duplicado: '{}'", peticion.dni());
            throw new ReglaNegocioException("Ya existe un cliente registrado con el DNI: " + peticion.dni());
        }

        if (clienteRepository.existsByEmail(peticion.email())) {
            log.warn("Intento de registro con email duplicado: '{}'", peticion.email());
            throw new ReglaNegocioException("Ya existe un cliente registrado con el email: " + peticion.email());
        }
    }

    /**
     * Valida que los datos únicos del cliente (DNI y Email) no colisionen con otros clientes al editar.
     *
     * @param id Identificador UUID del cliente en edición.
     * @param peticion Datos nuevos a verificar.
     * @throws ReglaNegocioException si el DNI o Email ya pertenecen a otro cliente.
     */
    public void validarParaActualizacion(UUID id, ClienteRequestDto peticion) {
        if (peticion == null) {
            throw new IllegalArgumentException("Los datos del cliente no pueden ser nulos.");
        }

        if (clienteRepository.existsByDniAndIdNot(peticion.dni(), id)) {
            log.warn("Conflicto al actualizar: El DNI '{}' ya pertenece a otro cliente", peticion.dni());
            throw new ReglaNegocioException("Ya existe otro cliente registrado con el DNI: " + peticion.dni());
        }

        if (clienteRepository.existsByEmailAndIdNot(peticion.email(), id)) {
            log.warn("Conflicto al actualizar: El email '{}' ya pertenece a otro cliente", peticion.email());
            throw new ReglaNegocioException("Ya existe otro cliente registrado con el email: " + peticion.email());
        }
    }
}
