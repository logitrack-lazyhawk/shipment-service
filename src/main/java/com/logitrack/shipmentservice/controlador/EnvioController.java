package com.logitrack.shipmentservice.controlador;

import com.logitrack.shipmentservice.dominio.dto.EnvioRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.servicio.EnvioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final EnvioServicio envioServicio;

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

    /**
     * Consulta el detalle de un envío específico utilizando su identificador único.
     *
     * @param id Identificador UUID del envío.
     * @return El detalle completo del paquete.
     */
    @Operation(summary = "Obtener un envío por su UUID")
    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponseDto> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(envioServicio.obtenerPorId(id));
    }

    /**
     * Consulta el detalle de un envío utilizando su código de seguimiento (Tracking Code).
     *
     * @param codigo Código de seguimiento público (Ej. LT-XXXXXX-XXXXX).
     * @return El detalle completo del paquete.
     */
    @Operation(summary = "Rastrear un envío por su código de seguimiento")
    @GetMapping("/tracking/{codigo}")
    public ResponseEntity<EnvioResponseDto> obtenerPorTracking(@PathVariable String codigo) {
        return ResponseEntity.ok(envioServicio.obtenerPorTracking(codigo));
    }

    /**
     * Obtiene la lista de envíos registrados en el sistema, permitiendo un filtrado opcional por estado.
     *
     * @param estado Estado operativo del envío a filtrar (opcional).
     * @return Lista de envíos que coinciden con el criterio.
     */
    @Operation(summary = "Listar envíos registrados (con filtrado opcional por estado)")
    @GetMapping
    public ResponseEntity<List<EnvioResponseDto>> listarEnvios(@RequestParam(required = false) EstadoEnvio estado) {
        return ResponseEntity.ok(envioServicio.listarEnvios(estado));
    }

}
