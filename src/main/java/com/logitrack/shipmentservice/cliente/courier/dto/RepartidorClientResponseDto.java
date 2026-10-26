package com.logitrack.shipmentservice.cliente.courier.dto;

import java.util.UUID;

/**
 * DTO que representa la respuesta recibida desde courier-service al consultar la disponibilidad de un repartidor.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public record RepartidorClientResponseDto(
        UUID id,
        String nombre,
        String dni,
        String telefono,
        String email,
        String tipoVehiculo,
        String estado
) {}
