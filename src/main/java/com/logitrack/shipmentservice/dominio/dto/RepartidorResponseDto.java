package com.logitrack.shipmentservice.dominio.dto;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import java.util.UUID;

/**
 * Objeto de transferencia utilizado para enviar los datos de un repartidor como respuesta.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public record RepartidorResponseDto(
        UUID id,
        String nombre,
        String tipoVehiculo,
        EstadoRepartidor estado
) {}
