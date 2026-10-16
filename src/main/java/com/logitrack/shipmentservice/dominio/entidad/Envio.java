package com.logitrack.shipmentservice.dominio.entidad;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.valueObject.Direccion;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad principal que representa un paquete o envío dentro del sistema LogiTrack.
 * Gestiona la información física, las direcciones y su ciclo de vida operativo.
 *
 * @author luisMPlus, 95670446-maker y leoM2022 - Equipo lazyHawk
 * @version 1.0.0
 */
@Entity
@Table(name = "envios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Envio extends EntidadAuditable {

    @Column(name = "codigo_seguimiento", nullable = false, unique = true, length = 50)
    private String codigoSeguimiento;

    @Column(name = "peso", nullable = false)
    private Double peso;

    @Column(name = "ancho", nullable = false)
    private Double ancho;

    @Column(name = "alto", nullable = false)
    private Double alto;

    @Column(name = "largo", nullable = false)
    private Double largo;

    @Column(name = "tarifa", nullable = false)
    private Double tarifa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoEnvio estado;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calle", column = @Column(name = "origen_calle", length = 150)),
            @AttributeOverride(name = "numero", column = @Column(name = "origen_numero", length = 20)),
            @AttributeOverride(name = "ciudad", column = @Column(name = "origen_ciudad", length = 100)),
            @AttributeOverride(name = "codigoPostal", column = @Column(name = "origen_cp", length = 20)),
            @AttributeOverride(name = "provincia", column = @Column(name = "origen_provincia", length = 100)),
            @AttributeOverride(name = "localidad", column = @Column(name = "origen_localidad", length = 100)),
            @AttributeOverride(name = "descripcion", column = @Column(name = "origen_descripcion", length = 255))
    })
    private Direccion origen;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calle", column = @Column(name = "destino_calle", length = 150)),
            @AttributeOverride(name = "numero", column = @Column(name = "destino_numero", length = 20)),
            @AttributeOverride(name = "ciudad", column = @Column(name = "destino_ciudad", length = 100)),
            @AttributeOverride(name = "codigoPostal", column = @Column(name = "destino_cp", length = 20)),
            @AttributeOverride(name = "provincia", column = @Column(name = "destino_provincia", length = 100)),
            @AttributeOverride(name = "localidad", column = @Column(name = "destino_localidad", length = 100)),
            @AttributeOverride(name = "descripcion", column = @Column(name = "destino_descripcion", length = 255))
    })
    private Direccion destino;

    /**
     * TODO: Implementar relación ManyToOne con Cliente cuando se modele dicha entidad (FetchType.LAZY).
     * TODO: Implementar relación ManyToOne con Repartidor para la asignación (FetchType.LAZY).
     * TODO: Integrar lógica de validación de pago antes de despachar el envío.
     */

    /**
     * Calcula la tarifa del envío basándose en el peso y las dimensiones del paquete.
     *
     * @return El costo total calculado.
     */
    public Double calcularTarifa() {
        return (this.peso * 1.5) + (this.ancho * this.alto * this.largo * 0.1);
    }

    /**
     * Valida si el envío se encuentra en un estado válido para ser cancelado.
     *
     * @return Verdadero si el estado es estrictamente PENDIENTE.
     */
    public Boolean puedeCancelarse() {
        return this.estado == EstadoEnvio.PENDIENTE;
    }

    /**
     * Ejecuta la transición de estado hacia CANCELADO si las reglas de negocio lo permiten.
     *
     * @throws IllegalStateException si el paquete ya ha avanzado en su ciclo de vida.
     */
    public void cancelar() {
        if (Boolean.TRUE.equals(puedeCancelarse())) {
            this.estado = EstadoEnvio.CANCELADO;
        } else {
            throw new IllegalStateException("Error de dominio: El envío no se encuentra en estado PENDIENTE.");
        }
    }

    /**
     * Coordina la transición operativa de los estados del paquete.
     *
     * @param nuevoEstado El estado de destino.
     */
    public void cambiarEstado(EstadoEnvio nuevoEstado) {
        // TODO: Desarrollar la validación estricta de la máquina de estados.
        this.estado = nuevoEstado;
    }
}