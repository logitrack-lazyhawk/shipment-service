package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.EnvioRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;

import java.util.List;

/**
 * Contrato del servicio de apicacion para la gestion de envios
 * @author LuisMPlus, 95670446-maker y leoM2022 - Equipo lazyHawk
 * @version 1.0.0
 */
public interface EnvioServicio {
    EnvioResponseDto registrarEnvio(EnvioRequestDto peticion);
    EnvioResponseDto obtenerPorId(String id);
    EnvioResponseDto obtenerPorTracking(String codigo);
    List<EnvioResponseDto> listarEnvios(EstadoEnvio estado);

}
