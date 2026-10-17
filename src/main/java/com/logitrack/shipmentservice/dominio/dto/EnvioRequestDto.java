package com.logitrack.shipmentservice.dominio.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Objeto de transferencia utilizado para recibir la peticion de creacion de un envio.
 * No incluye tarifa ni estado, ya que el sistema los calcula e inicializa.
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public record EnvioRequestDto(
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
