package com.logitrack.shipmentservice.repositorio;

import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.entidad.HistorialEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialEnvioRepository extends JpaRepository<HistorialEnvio, UUID> {
    List<HistorialEnvio> findByEnvioIdOrderByFechaHoraDesc(UUID envioId);
    List<HistorialEnvio> findByEnvioOrderByFechaHoraDesc(Envio envio);
}
