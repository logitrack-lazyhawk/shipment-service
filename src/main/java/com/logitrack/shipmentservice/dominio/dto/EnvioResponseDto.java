package com.logitrack.shipmentservice.dominio.dto;
/**
 * Objeto de transferencia utilizado para enviar la respuesta de un envio.
 * Incluye todos los datos del envio, incluyendo tarifa y estado.
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;

public record EnvioResponseDto(
        String id,
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
}
