package com.logitrack.shipmentservice.dominio.entidad;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

/**
 * Entidad que representa al personal de entrega (repartidor) en la plataforma LogiTrack.
 * Soporta soft delete automático basado en timestamp y gestión de estado operativo.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Entity
@Table(name = "repartidores")
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "fecha_eliminacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repartidor extends EntidadAuditable {

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo_vehiculo", nullable = false, length = 50)
    private String tipoVehiculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoRepartidor estado = EstadoRepartidor.ACTIVO;

    /**
     * Marca al repartidor como inactivo.
     */
    public void desactivar() {
        this.estado = EstadoRepartidor.INACTIVO;
    }

    /**
     * Marca al repartidor como activo.
     */
    public void activar() {
        this.estado = EstadoRepartidor.ACTIVO;
    }
}
