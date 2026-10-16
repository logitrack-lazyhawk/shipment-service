package com.logitrack.shipmentservice.dominio.dto;
/**
 * Objeto de transferencia para encapsular los datos de una direccion origen
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo LogiTrack
 * @version 1.0.0
 */
public record DireccionDto(
        String calle,
        String numero,
        String ciudad,
        String codigoPostal,
        String provincia,
        String localidad,
        String descripcion
) {
}
