package com.logitrack.shipmentservice.servicio.mapper;

import com.logitrack.shipmentservice.dominio.dto.ClienteRequestDto;
import com.logitrack.shipmentservice.dominio.dto.ClienteResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Cliente;
import org.springframework.stereotype.Component;

/**
 * Componente responsable de la transformación bidireccional entre la entidad Cliente y sus DTOs.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Component
public class ClienteMapper {

    /**
     * Convierte un DTO de creación en la entidad de dominio Cliente.
     *
     * @param dto Datos recibidos en la petición.
     * @return Entidad Cliente lista para persistir.
     */
    public Cliente toEntity(ClienteRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return Cliente.builder()
                .nombre(dto.nombre())
                .dni(dto.dni())
                .telefono(dto.telefono())
                .email(dto.email())
                .build();
    }

    /**
     * Actualiza los datos de una entidad Cliente existente con los valores del DTO.
     *
     * @param entity Entidad a actualizar.
     * @param dto Datos nuevos.
     */
    public void actualizarEntidad(Cliente entity, ClienteRequestDto dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setNombre(dto.nombre());
        entity.setDni(dto.dni());
        entity.setTelefono(dto.telefono());
        entity.setEmail(dto.email());
    }

    /**
     * Convierte la entidad Cliente en un DTO de respuesta.
     *
     * @param entity Entidad obtenida de la base de datos.
     * @return DTO con la información pública del cliente.
     */
    public ClienteResponseDto toDto(Cliente entity) {
        if (entity == null) {
            return null;
        }

        return new ClienteResponseDto(
                entity.getId(),
                entity.getNombre(),
                entity.getDni(),
                entity.getTelefono(),
                entity.getEmail()
        );
    }
}
