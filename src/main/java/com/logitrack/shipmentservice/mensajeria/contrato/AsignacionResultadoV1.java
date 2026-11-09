
package com.logitrack.shipmentservice.mensajeria.contrato;

import java.time.Instant;
import java.util.UUID;
/**
 * Representa el resultado de una asignación de envío.
 * Contiene información sobre el evento, la solicitud, el envío y el repartidor.
 * @version 1.0.0
 * @author Equipo de LazyHawks
 */
public record AsignacionResultadoV1(
        UUID eventoId,
        UUID solicitudId,
        int version,
        Instant ocurridoEn,
        UUID envioId,
        UUID repartidorId,
        boolean aceptada,
        String motivo
) {}
