package com.logitrack.shipmentservice.dominio.dto;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Objeto de transferencia utilizado para enviar la respuesta de un envío.
 * Incluye todos los datos del envío, tarifa, estado y el repartidor asignado si corresponde.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Schema(description = "Respuesta detallada con información de un envío")
public record EnvioResponseDto(
        @Schema(description = "Identificador único UUID del envío", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID id,

        @Schema(description = "Código de seguimiento único generado", example = "LT-458921-A3F9B")
        String codigoSeguimiento,

        @Schema(description = "Peso en kilogramos", example = "5.5")
        Double peso,

        @Schema(description = "Ancho en centímetros", example = "20.0")
        Double ancho,

        @Schema(description = "Alto en centímetros", example = "15.0")
        Double alto,

        @Schema(description = "Largo en centímetros", example = "30.0")
        Double largo,

        @Schema(description = "Tarifa calculada del envío", example = "42.75")
        Double tarifa,

        @Schema(description = "Estado actual del ciclo de vida del envío", example = "PENDIENTE")
        EstadoEnvio estado,

        @Schema(description = "Dirección de origen del envío")
        DireccionDto origen,

        @Schema(description = "Dirección de destino del envío")
        DireccionDto destino,

        @Schema(description = "Identificador UUID del repartidor asignado, o null si aún no se asignó", example = "11111111-1111-1111-1111-111111111111")
        UUID repartidorId
) {
    public EnvioResponseDto(
            UUID id,
            String codigoSeguimiento,
            Double peso,
            Double ancho,
            Double alto,
            Double largo,
            Double tarifa,
            EstadoEnvio estado,
            DireccionDto origen,
            DireccionDto destino
    ) {
        this(id, codigoSeguimiento, peso, ancho, alto, largo, tarifa, estado, origen, destino, null);
    }
}
