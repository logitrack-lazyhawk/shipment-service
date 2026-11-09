
package com.logitrack.shipmentservice.dominio.dto;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoSolicitudAsignacion;

import java.util.UUID;

/*
    DTO que representa la respuesta de una solicitud de asignación.
    Contiene información sobre la solicitud, el envío, el repartidor y el estado de la solicitud.
    @author: Equipo de LazyHawks
    @version: 1.0.0
 */
public record SolicitudAsignacionResponseDto(
        UUID solicitudId,
        UUID envioId,
        UUID repartidorId,
        EstadoSolicitudAsignacion estado
) {}
