
package com.logitrack.shipmentservice.mensajeria.contrato;

import java.time.Instant;
import java.util.UUID;

/*
    Registro que representa la información de una solicitud de asignación.
    Contiene los siguientes campos:
    - eventoId: Identificador único del evento.
    - solicitudId: Identificador único de la solicitud.
    - version: Versión del evento.
    - ocurridoEn: Fecha y hora en que ocurrió el evento.
    - envioId: Identificador único del envío asociado a la solicitud.
    - repartidorId: Identificador único del repartidor asignado a la solicitud.
    @author: Equipo de LazyHawks
    @version: 1.0.0
 */
public record AsignacionSolicitadaV1(
        UUID eventoId,
        UUID solicitudId,
        int version,
        Instant ocurridoEn,
        UUID envioId,
        UUID repartidorId
) {}
