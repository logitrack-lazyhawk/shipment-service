package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.EnvioRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;

import java.util.List;
import java.util.UUID;

/**
 * Contrato del servicio de aplicación para la gestión de envíos.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 - Equipo lazyHawk
 * @version 1.0.0
 */
public interface EnvioServicio {
    EnvioResponseDto registrarEnvio(EnvioRequestDto peticion);
    EnvioResponseDto obtenerPorId(UUID id);
    EnvioResponseDto obtenerPorTracking(String codigo);
    List<EnvioResponseDto> listarEnvios(EstadoEnvio estado);
    EnvioResponseDto cancelarEnvio(UUID id);
    EnvioResponseDto cambiarEstado(UUID id, EstadoEnvio nuevoEstado);
}
