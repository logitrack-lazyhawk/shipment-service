package com.logitrack.shipmentservice.dominio.enumerador;
/**
 * Enumeracion que define los estados permitidos en el ciclo de vida de un paquete.
 * La misma debe respetar el flujo de estados: PENDIENTE -> EN_RUTA -> ENTREGADO
 * o PENDIENTE -> CANCELADO.
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo LogiTrack
 * @version 1.0.0
 */

public enum EstadoEnvio {
    PENDIENTE,
    EN_RUTA,
    ENTREGADO,
    CANCELADO
}
