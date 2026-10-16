package com.logitrack.shipmentservice.dominio.dto;
/**
 * Objeto de transferencia utilizado para recibir la peticion de creacion de un envio.
 * No incluye tarifa ni estado, ya que el sistema los calcula e inicializa.
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo LogiTrack
 * @version 1.0.0
 */
public record EnvioRequestDto(
        Double peso,
        Double ancho,
        Double alto,
        Double largo,
        DireccionDto origen,
        DireccionDto destino
) {
}
