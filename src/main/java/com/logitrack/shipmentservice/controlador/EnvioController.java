package com.logitrack.shipmentservice.controlador;

import com.logitrack.shipmentservice.dominio.dto.EnvioRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.servicio.EnvioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestion principal de envios.
 * Expone la interfaz web para el registro, calculo de tarifas y seguimiento de paquetes
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
@Tag(name = "Gestion de envios", description = "Endpoints transaccionales para el ciclo de vida de los paquetes")
public class EnvioController {
    private final EnvioServicio envioService;
    /**
     * Registra un nuevo envío en la plataforma LogiTrack.
     * Genera automáticamente el Hash único de seguimiento y establece el estado en PENDIENTE.
     *
     * @param peticion Objeto de transferencia con dimensiones, peso y direcciones de origen y destino.
     * @return El envío persistido con su UUID, código Hash de seguimiento y tarifa calculada.
     */
    @Operation(summary = "Registrar un nuevo envío y calcular su tarifa operativa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envío registrado exitosamente en la base de datos"),
            @ApiResponse(responseCode = "400", description = "Los datos enviados en la petición no son válidos")
    })
    @PostMapping
    public ResponseEntity<EnvioResponseDto> registrarEnvio(@RequestBody EnvioRequestDto peticion) {
        EnvioResponseDto respuesta = envioService.registrarEnvio(peticion);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

}
