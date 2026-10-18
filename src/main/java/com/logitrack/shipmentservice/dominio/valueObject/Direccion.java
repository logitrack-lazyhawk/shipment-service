package com.logitrack.shipmentservice.dominio.valueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
/**
 * Value Object que representa la dirección de origen o destino de un envío.
 * Se utiliza como un objeto embebido dentro de la entidad Envio.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class Direccion {

    @Column(name = "calle", nullable = false, length = 150)
    private String calle;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @Column(name = "codigo_postal", nullable = false, length = 20)
    private String codigoPostal;

    @Column(name = "provincia", nullable = false, length = 100)
    private String provincia;

    @Column(name = "localidad", nullable = false, length = 100)
    private String localidad;

    @Column(name = "descripcion", nullable = true, length = 255)
    private String descripcion;
}
