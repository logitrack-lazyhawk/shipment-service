package com.logitrack.shipmentservice.dominio.dto;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequestDto(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoEnvio nuevoEstado
) {}
