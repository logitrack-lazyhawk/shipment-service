package com.logitrack.shipmentservice.dominio.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Superclase abstracta que modela los atributos comunes de una persona (repartidor, cliente)
 * centralizando datos de identificación civil y contacto.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class Persona extends EntidadAuditable {

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "dni", nullable = false, length = 20)
    private String dni;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;
}
