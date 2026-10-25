package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.ClienteRequestDto;
import com.logitrack.shipmentservice.dominio.dto.ClienteResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato del servicio de aplicación para la gestión de clientes.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public interface ClienteServicio {
    ClienteResponseDto registrarCliente(ClienteRequestDto peticion);
    ClienteResponseDto obtenerPorId(UUID id);
    Page<ClienteResponseDto> listarClientes(Pageable pageable);
    ClienteResponseDto actualizarCliente(UUID id, ClienteRequestDto peticion);
    void eliminarCliente(UUID id);
}
