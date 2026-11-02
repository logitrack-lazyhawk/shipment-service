package com.logitrack.shipmentservice.dominio.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

/**
 * Entidad que representa a un cliente emisor o receptor en LogiTrack.
 * Soporta soft delete automático basado en timestamp y hereda los atributos de auditoría (UUID y fechas).
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.1.0
 */
@Entity
@Table(name = "clientes")
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "fecha_eliminacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Cliente extends EntidadAuditable {

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "dni", nullable = false, length = 20)
    private String dni;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @Column(name = "email", nullable = false, length = 150)
    private String email;
}