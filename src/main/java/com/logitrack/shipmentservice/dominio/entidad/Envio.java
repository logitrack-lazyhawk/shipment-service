package com.logitrack.shipmentservice.dominio.entidad;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.dominio.exception.TransicionEstadoInvalidaException;
import com.logitrack.shipmentservice.dominio.valueObject.Direccion;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad principal que representa un paquete o envío dentro del sistema LogiTrack.
 * Gestiona la información física, las direcciones, su ciclo de vida operativo,
 * las asociaciones con repartidor/cliente y su historial de auditoría.
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
@SuperBuilder
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

    @Column(name = "repartidor_id", columnDefinition = "binary(16)")
    private UUID repartidorId;

    @NotNull(message = "El cliente asociado al envío es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, columnDefinition = "binary(16)")
    private Cliente cliente;

    @OneToMany(mappedBy = "envio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HistorialEnvio> historial = new ArrayList<>();

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
     * @throws TransicionEstadoInvalidaException si el paquete ya ha avanzado en su ciclo de vida.
     */
    public void cancelar() {
        if (Boolean.TRUE.equals(puedeCancelarse())) {
            this.estado = EstadoEnvio.CANCELADO;
        } else {
            throw new TransicionEstadoInvalidaException(
                    "Error de dominio: El envío no se encuentra en estado PENDIENTE para ser cancelado. Estado actual: " + this.estado
            );
        }
    }

    /**
     * Coordina la transición operativa de los estados del paquete.
     * Aplica la máquina de estados estricta: PENDIENTE -> EN_RUTA -> ENTREGADO (o CANCELADO).
     *
     * @param nuevoEstado El estado de destino.
     * @throws TransicionEstadoInvalidaException si el salto de estado no está permitido.
     */
    public void cambiarEstado(EstadoEnvio nuevoEstado) {
        if (nuevoEstado == null) {
            throw new TransicionEstadoInvalidaException("El nuevo estado no puede ser nulo.");
        }

        // Validamos según el estado actual
        switch (this.estado) {
            case PENDIENTE:
                if (nuevoEstado != EstadoEnvio.EN_RUTA && nuevoEstado != EstadoEnvio.CANCELADO) {
                    throw new TransicionEstadoInvalidaException(
                            "Transición inválida: De PENDIENTE solo se puede pasar a EN_RUTA o CANCELADO. Se intentó: " + nuevoEstado
                    );
                }
                break;

            case EN_RUTA:
                if (nuevoEstado != EstadoEnvio.ENTREGADO) {
                    throw new TransicionEstadoInvalidaException(
                            "Transición inválida: De EN_RUTA solo se puede pasar a ENTREGADO. Se intentó: " + nuevoEstado
                    );
                }
                break;

            case ENTREGADO:
            case CANCELADO:
                throw new TransicionEstadoInvalidaException(
                        "Transición inválida: El envío ya está en un estado final (" + this.estado + ") y no puede modificarse."
                );

            default:
                throw new TransicionEstadoInvalidaException("Estado actual no reconocido.");
        }

        // Si pasó todas las validaciones, actualizamos el estado
        this.estado = nuevoEstado;
    }

    /**
     * Asigna un repartidor al envío validando las reglas de negocio:
     * - El envío debe encontrarse en un estado previo a EN_RUTA (estrictamente PENDIENTE).
     * - El identificador del repartidor no debe ser nulo.
     *
     * @param repartidorId Identificador UUID del repartidor a vincular.
     * @throws ReglaNegocioException si no se cumplen las condiciones.
     */
    public void asignarRepartidor(UUID repartidorId) {
        if (this.estado != EstadoEnvio.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Operación inválida: No se puede asignar un repartidor a un envío en estado " + this.estado + ". Debe encontrarse en estado PENDIENTE."
            );
        }
        if (repartidorId == null) {
            throw new ReglaNegocioException("Error de validación: El repartidor a asignar no puede ser nulo.");
        }
        this.repartidorId = repartidorId;
    }
}
