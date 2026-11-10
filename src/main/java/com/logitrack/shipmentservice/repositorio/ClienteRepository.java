
package com.logitrack.shipmentservice.repositorio;

import com.logitrack.shipmentservice.dominio.entidad.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
/**
 * Repositorio para la entidad Cliente.
 * Proporciona métodos para realizar operaciones CRUD y consultas personalizadas en la base de datos.
 * @author Logitrack
 * @version 1.0.0
 **/
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByDni(String dni);

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByEmailIgnoreCase(String email);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    boolean existsByDniAndIdNot(String dni, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
