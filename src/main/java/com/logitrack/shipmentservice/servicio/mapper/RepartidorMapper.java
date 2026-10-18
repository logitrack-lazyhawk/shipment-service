package com.logitrack.shipmentservice.servicio.mapper;

import com.logitrack.shipmentservice.dominio.dto.RepartidorRequestDto;
import com.logitrack.shipmentservice.dominio.dto.RepartidorResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Repartidor;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import org.springframework.stereotype.Component;

/**
 * Componente responsable de la transformación bidireccional entre la entidad Repartidor y sus DTOs.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Component
public class RepartidorMapper {

    /**
     * Convierte un DTO de creación en la entidad de dominio Repartidor.
     * Si no se define el estado, por defecto se inicializa en ACTIVO.
     *
     * @param dto Datos recibidos en la petición.
     * @return Entidad Repartidor lista para persistir.
     */
    public Repartidor toEntity(RepartidorRequestDto dto) {
        if (dto == null) {
            return null;
        }

        EstadoRepartidor estadoInicial = dto.estado() != null
                ? dto.estado()
                : EstadoRepartidor.ACTIVO;

        return Repartidor.builder()
                .nombre(dto.nombre())
                .tipoVehiculo(dto.tipoVehiculo())
                .estado(estadoInicial)
                .build();
    }

    /**
     * Convierte la entidad Repartidor en un DTO de respuesta.
     *
     * @param entity Entidad obtenida de la base de datos.
     * @return DTO con la información pública del repartidor.
     */
    public RepartidorResponseDto toDto(Repartidor entity) {
        if (entity == null) {
            return null;
        }

        return new RepartidorResponseDto(
                entity.getId(),
                entity.getNombre(),
                entity.getTipoVehiculo(),
                entity.getEstado()
        );
    }
}
