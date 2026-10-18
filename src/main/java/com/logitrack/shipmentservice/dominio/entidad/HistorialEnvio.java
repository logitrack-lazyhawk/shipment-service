package com.logitrack.shipmentservice.dominio.entidad;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historial_envios")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEnvio {

    @Id
    @Column(name = "id", columnDefinition = "binary(16)")
    private UUID id;

    @Column(name = "envio_id", nullable = false, columnDefinition = "binary(16)")
    private UUID envioId;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private String estadoNuevo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "usuario_actor", length = 100)
    private String usuarioActor;

    // TODO: Si se necesita, podemos agregar una relación @ManyToOne real al Envio,
    // pero guardar el envioId es más eficiente para auditoría.
}
