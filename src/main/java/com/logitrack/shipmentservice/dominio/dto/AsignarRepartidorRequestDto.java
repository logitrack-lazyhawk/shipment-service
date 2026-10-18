package com.logitrack.shipmentservice.dominio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Objeto de transferencia utilizado para recibir la solicitud de asignación de un repartidor a un envío.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Schema(description = "DTO para solicitar la asignación de un repartidor a un paquete")
public record AsignarRepartidorRequestDto(
        @Schema(description = "UUID del repartidor activo a vincular", example = "11111111-1111-1111-1111-111111111111")
        @NotNull(message = "El identificador del repartidor es obligatorio")
        UUID repartidorId
) {}
