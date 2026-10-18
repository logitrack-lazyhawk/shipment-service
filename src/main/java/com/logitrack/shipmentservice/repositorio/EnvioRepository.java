package com.logitrack.shipmentservice.repositorio;

import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la gestión de persistencia de la entidad Envio.
 * Utiliza Spring Data JPA para abstraer las consultas SQL.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Repository
public interface EnvioRepository extends JpaRepository<Envio, UUID> {
    boolean existsByCodigoSeguimiento(String codigoSeguimiento);

    Optional<Envio> findByCodigoSeguimiento(String codigoSeguimiento);

    List<Envio> findByEstado(EstadoEnvio estado);
}
