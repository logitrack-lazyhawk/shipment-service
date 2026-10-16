package com.logitrack.shipmentservice.dominio.entidad;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
/**
 * Superclase abastracta que provee el identificador UUID y los atributos de auditoria
 * para registrar de forma automatica la creacion y actualizacion de los registros.
 * Aplicamos el patron MappedSuperclass para delegar sus columnas a las tablas hijas.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022
 * @version 1.0.0
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
