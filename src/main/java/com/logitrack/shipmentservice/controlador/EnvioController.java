package com.logitrack.shipmentservice.controlador;

import com.logitrack.shipmentservice.dominio.dto.AsignarRepartidorRequestDto;
import com.logitrack.shipmentservice.dominio.dto.CambiarEstadoRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.servicio.EnvioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión principal de envíos.
 * Expone la interfaz web para el registro, cálculo de tarifas, seguimiento y asignación de repartidores.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/envios")
@RequiredArgsConstructor
@Tag(name = "Gestion de envios", description = "Endpoints transaccionales para el ciclo de vida de los paquetes")
public class EnvioController {

    private final EnvioServicio envioServicio;

    /**
     * Registra un nuevo envío en la plataforma LogiTrack.
     * Genera automáticamente el Hash único de seguimiento y establece el estado en PENDIENTE.
     *
     * @param peticion Datos dimensionales y de ruteo del paquete.
     * @return El envío registrado con su código de seguimiento generado.
     */
    @Operation(summary = "Crear y registrar un nuevo envío")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envío registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos en la petición")
    })
    @PostMapping
    public ResponseEntity<EnvioResponseDto> registrarEnvio(@Valid @RequestBody EnvioRequestDto peticion) {
        EnvioResponseDto respuesta = envioServicio.registrarEnvio(peticion);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Recupera la información detallada de un envío a partir de su ID.
     *
     * @param id Identificador UUID del paquete.
     * @return Los datos del envío si existe.
     */
    @Operation(summary = "Obtener un envío por su UUID")
    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponseDto> obtenerPorId(@PathVariable UUID id) {
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

    /**
     * Consulta la lista paginada de envíos registrados, permitiendo un filtrado opcional por estado.
     *
     * @param estado Estado operativo del envío a filtrar (opcional).
     * @param pageable Configuración de paginación (page, size, sort). Por defecto página 0 y tamaño 10.
     * @return Página de envíos que coinciden con el criterio.
     */
    @Operation(summary = "Listar envíos paginados (con filtrado opcional por estado)")
    @GetMapping({"/paginados", "/paginado"})
    public ResponseEntity<Page<EnvioResponseDto>> listarEnviosPaginados(
            @RequestParam(required = false) EstadoEnvio estado,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(envioServicio.listarEnviosPaginados(estado, pageable));
    }

    /**
     * Anula un envío registrado en el sistema.
     * Solo procede si el paquete se encuentra estrictamente en estado PENDIENTE.
     *
     * @param id Identificador UUID del envío a cancelar.
     * @return El detalle del envío con su estado actualizado a CANCELADO.
     */
    @Operation(summary = "Cancelar un envío (Solo aplicable en estado PENDIENTE)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envío cancelado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El envío ya no puede ser cancelado por su estado actual"),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<EnvioResponseDto> cancelarEnvio(@PathVariable UUID id) {
        return ResponseEntity.ok(envioServicio.cancelarEnvio(id));
    }

    @Operation(summary = "Cambiar el estado de un envío")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
            @ApiResponse(responseCode = "404", description = "Envío no encontrado")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDto> cambiarEstado(
            @PathVariable UUID id,
            @RequestBody @Valid CambiarEstadoRequestDto request) {
        return ResponseEntity.ok(envioServicio.cambiarEstado(id, request.nuevoEstado()));
    }

    /**
     * Asigna un repartidor a un paquete en estado PENDIENTE.
     *
     * @param id Identificador UUID del envío a actualizar.
     * @param request Cuerpo con el ID del repartidor que tomará el paquete.
     * @return El envío con el repartidor asignado.
     */
    @Operation(summary = "Asignar un repartidor a un paquete antes de iniciar el viaje")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repartidor asignado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Reglas de negocio no cumplidas o repartidor inactivo"),
            @ApiResponse(responseCode = "404", description = "Envío o repartidor no encontrado")
    })
    @PostMapping("/{id}/asignar-repartidor")
    public ResponseEntity<EnvioResponseDto> asignarRepartidor(
            @PathVariable UUID id,
            @RequestBody @Valid AsignarRepartidorRequestDto request) {
        return ResponseEntity.ok(envioServicio.asignarRepartidor(id, request.repartidorId()));
    }
}
