package com.logitrack.shipmentservice.controlador;

import com.logitrack.shipmentservice.dominio.dto.RepartidorRequestDto;
import com.logitrack.shipmentservice.dominio.dto.RepartidorResponseDto;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import com.logitrack.shipmentservice.servicio.RepartidorServicio;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST para la gestión de repartidores.
 * Expone la interfaz web para el alta, consulta y eliminación del personal logístico.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/repartidores")
@RequiredArgsConstructor
@Tag(name = "Gestion de repartidores", description = "Endpoints para registro, consulta y baja del personal de entrega")
public class RepartidorController {

    private final RepartidorServicio repartidorServicio;

    /**
     * Registra un nuevo repartidor en la plataforma con estado inicial configurable (ACTIVO o INACTIVO).
     *
     * @param peticion Datos del repartidor (nombre, tipoVehiculo, estado opcional).
     * @return El repartidor persistido con su UUID generado y estado asignado.
     */
    @Operation(summary = "Registrar un nuevo repartidor en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Repartidor registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Los datos enviados en la petición no son válidos")
    })
    @PostMapping
    public ResponseEntity<RepartidorResponseDto> registrarRepartidor(@Valid @RequestBody RepartidorRequestDto peticion) {
        RepartidorResponseDto respuesta = repartidorServicio.registrarRepartidor(peticion);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Consulta la lista paginada de repartidores registrados, permitiendo un filtrado opcional por estado.
     *
     * @param estado Estado operativo del repartidor a filtrar (ej. ACTIVO, INACTIVO) (opcional).
     * @param pageable Configuración de paginación (page, size, sort). Por defecto página 0 y tamaño 10.
     * @return Página de repartidores que coinciden con el criterio.
     */
    @Operation(summary = "Listar repartidores paginados (con filtrado opcional por estado)")
    @GetMapping
    public ResponseEntity<Page<RepartidorResponseDto>> listarRepartidores(
            @RequestParam(required = false) EstadoRepartidor estado,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(repartidorServicio.listarRepartidores(estado, pageable));
    }

    /**
     * Consulta el detalle de un repartidor específico utilizando su identificador único UUID.
     *
     * @param id Identificador UUID del repartidor.
     * @return El detalle completo del repartidor.
     */
    @Operation(summary = "Obtener un repartidor por su UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repartidor encontrado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Repartidor no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RepartidorResponseDto> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(repartidorServicio.obtenerPorId(id));
    }

    /**
     * Da de baja a un repartidor por su identificador UUID (soft delete automático).
     *
     * @param id Identificador UUID del repartidor a eliminar.
     * @return 204 No Content si se eliminó con éxito.
     */
    @Operation(summary = "Eliminar un repartidor por su UUID (baja lógica)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Repartidor eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Repartidor no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRepartidor(@PathVariable UUID id) {
        repartidorServicio.eliminarRepartidor(id);
        return ResponseEntity.noContent().build();
    }
}
