package com.logitrack.shipmentservice.dominio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Objeto de transferencia utilizado para recibir la petición de creación de un repartidor.
 * No incluye el identificador ni el estado, ya que son asignados e inicializados por el sistema.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public record RepartidorRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El tipo de vehículo es obligatorio")
        @Size(max = 50, message = "El tipo de vehículo no puede superar los 50 caracteres")
        String tipoVehiculo
) {}
