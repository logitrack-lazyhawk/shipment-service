
package com.logitrack.shipmentservice.controlador;

import com.logitrack.shipmentservice.dominio.dto.AsignarRepartidorRequestDto;
import com.logitrack.shipmentservice.dominio.dto.SolicitudAsignacionResponseDto;
import com.logitrack.shipmentservice.servicio.SolicitudAsignacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
/*
    Controlador REST que maneja las solicitudes de asignación de envíos.
    Proporciona un endpoint para solicitar la asignación de un repartidor a un envío.
    @author: Equipo de LazyHawks
    @version: 1.0.0
 */
@RestController
@RequestMapping("/api/v1/envios/{envioId}/solicitudes-asignacion")
@RequiredArgsConstructor
public class SolicitudAsignacionController {

    private final SolicitudAsignacionService solicitudService;

    @PostMapping
    public ResponseEntity<SolicitudAsignacionResponseDto> solicitar(
            @PathVariable UUID envioId,
            @Valid @RequestBody AsignarRepartidorRequestDto request
    ) {
        SolicitudAsignacionResponseDto respuesta =
                solicitudService.solicitarAsignacion(
                        envioId,
                        request.repartidorId()
                );

        return ResponseEntity.accepted().body(respuesta);
    }
}
