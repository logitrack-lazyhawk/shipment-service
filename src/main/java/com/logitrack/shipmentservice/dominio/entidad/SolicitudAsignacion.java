
package com.logitrack.shipmentservice.dominio.entidad;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoSolicitudAsignacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
/*
    Clase que representa una solicitud de asignación en el sistema de gestión de envíos.
    Contiene información sobre el envío, el repartidor asignado, el estado de la solicitud y la fecha de creación.
    @author: Equipo de LazyHawks
    @version: 1.0.0
 */
@Entity
@Table(
        name = "solicitudes_asignacion",
        indexes = @Index(
                name = "idx_solicitud_envio_estado",
                columnList = "envio_id,estado"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class SolicitudAsignacion {

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false,
            columnDefinition = "binary(16)"
    )
    private UUID id;

    @Column(
            name = "envio_id",
            nullable = false,
            columnDefinition = "binary(16)"
    )
    private UUID envioId;

    @Column(
            name = "repartidor_id",
            nullable = false,
            columnDefinition = "binary(16)"
    )
    private UUID repartidorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 32)
    private EstadoSolicitudAsignacion estado;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    public SolicitudAsignacion(
            UUID id,
            UUID envioId,
            UUID repartidorId,
            EstadoSolicitudAsignacion estado,
            Instant fechaCreacion
    ) {
        this.id = id;
        this.envioId = envioId;
        this.repartidorId = repartidorId;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }
}
