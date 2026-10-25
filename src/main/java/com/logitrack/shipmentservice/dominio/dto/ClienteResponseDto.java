package com.logitrack.shipmentservice.dominio.dto;

import java.util.UUID;

/**
 * Objeto de transferencia utilizado para enviar los datos de un cliente como respuesta.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public record ClienteResponseDto(
        UUID id,
        String nombre,
        String dni,
        String telefono,
        String email
) {}
