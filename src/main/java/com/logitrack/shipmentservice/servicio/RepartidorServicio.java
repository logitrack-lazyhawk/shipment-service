package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.RepartidorRequestDto;
import com.logitrack.shipmentservice.dominio.dto.RepartidorResponseDto;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato del servicio de aplicación para la gestión de repartidores.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public interface RepartidorServicio {
    RepartidorResponseDto registrarRepartidor(RepartidorRequestDto peticion);
    RepartidorResponseDto obtenerPorId(UUID id);
    Page<RepartidorResponseDto> listarRepartidores(EstadoRepartidor estado, Pageable pageable);
    void eliminarRepartidor(UUID id);
}
