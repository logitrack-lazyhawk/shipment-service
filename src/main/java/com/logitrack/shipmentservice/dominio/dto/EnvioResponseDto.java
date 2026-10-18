package com.logitrack.shipmentservice.dominio.dto;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import java.util.UUID;

/**
 * Objeto de transferencia utilizado para enviar la respuesta de un envío.
 * Incluye todos los datos del envío, incluyendo tarifa y estado.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public record EnvioResponseDto(
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
) {}
