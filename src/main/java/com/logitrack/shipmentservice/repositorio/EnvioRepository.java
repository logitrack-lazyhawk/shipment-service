package com.logitrack.shipmentservice.repositorio;

import com.logitrack.shipmentservice.dominio.entidad.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestion de persistencia de la entidad Envio.
 * Utiliza Spring Data JPA para abstraer las consultas SQL
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */

@Repository
public interface EnvioRepository extends JpaRepository<Envio, String> {
    boolean existsByCodigoSeguimiento(String codigoSeguimiento);
}
