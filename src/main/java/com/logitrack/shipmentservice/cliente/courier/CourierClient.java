package com.logitrack.shipmentservice.cliente.courier;

import com.logitrack.shipmentservice.cliente.courier.dto.RepartidorClientResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Cliente declarativo Feign para la comunicación síncrona con el microservicio courier-service.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@FeignClient(name = "courier-service", url = "${app.services.courier.url:http://localhost:8081}")
public interface CourierClient {

    /**
     * Consulta la disponibilidad y estado de un repartidor en courier-service.
     *
     * @param id Identificador UUID del repartidor a consultar.
     * @return Los datos del repartidor si está activo y disponible.
     */
    @GetMapping("/api/v1/repartidores/{id}/disponibilidad")
    RepartidorClientResponseDto consultarDisponibilidad(@PathVariable("id") UUID id);
}
