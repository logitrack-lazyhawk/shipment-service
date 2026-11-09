
package com.logitrack.shipmentservice.repositorio;

import com.logitrack.shipmentservice.dominio.entidad.SolicitudAsignacion;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoSolicitudAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/*
    Repositorio para la entidad SolicitudAsignacion.
    Proporciona métodos para interactuar con la base de datos.
    @author: Equipo de LazyHawks
    @version: 1.0.0
 */

public interface SolicitudAsignacionRepository
        extends JpaRepository<SolicitudAsignacion, UUID> {

    boolean existsByEnvioIdAndEstado(
            UUID envioId,
            EstadoSolicitudAsignacion estado
    );
}
