package com.logitrack.shipmentservice.dominio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/**
 * Objeto de transferencia utilizado para recibir la peticion de creacion de un envio.
 * No incluye tarifa ni estado, ya que el sistema los calcula e inicializa.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public record EnvioRequestDto(
        @Schema(description = "Identificador único UUID del cliente solicitante", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        @NotNull(message = "El clienteId es obligatorio")
        UUID clienteId,

        @NotNull(message = "El peso es obligatorio")
        @Positive(message = "El peso debe ser mayor a cero")
        Double peso,

        @NotNull(message = "El ancho es obligatorio")
        Double ancho,

        @NotNull(message = "El alto es obligatorio")
        Double alto,

        @NotNull(message = "El largo es obligatorio")
        Double largo,

        @NotNull(message = "El origen es obligatorio")
        DireccionDto origen,

        @NotNull(message = "El destino es obligatorio")
        DireccionDto destino
) {}
