
package com.logitrack.shipmentservice.controlador;

import com.logitrack.shipmentservice.config.TrackingJwtVerifier;
import com.logitrack.shipmentservice.dominio.dto.CrearMiEnvioRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Cliente;
import com.logitrack.shipmentservice.repositorio.ClienteRepository;
import com.logitrack.shipmentservice.servicio.EnvioServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/envios/mis-envios")
@RequiredArgsConstructor
public class CrearMiEnvioController {

    private final TrackingJwtVerifier trackingJwtVerifier;
    private final ClienteRepository clienteRepository;
    private final EnvioServicio envioServicio;

    @PostMapping
    public ResponseEntity<EnvioResponseDto> crearMiEnvio(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorization,

            @Valid @RequestBody
            CrearMiEnvioRequestDto request
    ) {
        String email = trackingJwtVerifier
                .exigirEmailCliente(authorization);

        Cliente cliente = clienteRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNPROCESSABLE_ENTITY,
                                "No existe una ficha de cliente asociada "
                                        + "al usuario autenticado."
                        )
                );

        EnvioRequestDto peticion = new EnvioRequestDto(
                cliente.getId(),
                request.peso(),
                request.ancho(),
                request.alto(),
                request.largo(),
                request.origen().toDto(),
                request.destino().toDto()
        );

        EnvioResponseDto respuesta =
                envioServicio.registrarEnvio(peticion);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }
}
