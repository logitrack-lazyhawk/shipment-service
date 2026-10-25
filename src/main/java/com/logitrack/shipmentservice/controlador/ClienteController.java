package com.logitrack.shipmentservice.controlador;

import com.logitrack.shipmentservice.dominio.dto.ClienteRequestDto;
import com.logitrack.shipmentservice.dominio.dto.ClienteResponseDto;
import com.logitrack.shipmentservice.servicio.ClienteServicio;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST para la gestión de clientes.
 * Expone endpoints para el registro, consulta individual y paginada, edición y baja lógica de clientes.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Gestion de clientes", description = "Endpoints para registro, consulta, edición y baja de clientes")
public class ClienteController {

    private final ClienteServicio clienteServicio;

    /**
     * Registra un nuevo cliente en la plataforma.
     *
     * @param peticion Datos del cliente (nombre, dni, telefono, email).
     * @return El cliente persistido con su UUID generado.
     */
    @Operation(summary = "Registrar un nuevo cliente en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Los datos enviados en la petición no son válidos o están duplicados")
    })
    @PostMapping
    public ResponseEntity<ClienteResponseDto> registrarCliente(@Valid @RequestBody ClienteRequestDto peticion) {
        ClienteResponseDto respuesta = clienteServicio.registrarCliente(peticion);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Consulta la lista paginada de clientes registrados.
     *
     * @param pageable Configuración de paginación (page, size, sort). Por defecto página 0 y tamaño 10.
     * @return Página de clientes.
     */
    @Operation(summary = "Listar clientes de forma paginada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de clientes recuperada exitosamente")
    })
    @GetMapping
    public ResponseEntity<Page<ClienteResponseDto>> listarClientes(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        return ResponseEntity.ok(clienteServicio.listarClientes(pageable));
    }

    /**
     * Consulta el detalle de un cliente específico utilizando su identificador único UUID.
     *
     * @param id Identificador UUID del cliente.
     * @return El detalle completo del cliente.
     */
    @Operation(summary = "Obtener un cliente por su UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteServicio.obtenerPorId(id));
    }

    /**
     * Actualiza los datos de un cliente existente utilizando su identificador UUID.
     *
     * @param id Identificador UUID del cliente a editar.
     * @param peticion Nuevos datos del cliente (nombre, dni, telefono, email).
     * @return El cliente con los datos actualizados.
     */
    @Operation(summary = "Editar los datos de un cliente por su UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Los datos enviados en la petición no son válidos o colisionan con otro cliente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> actualizarCliente(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteRequestDto peticion) {
        return ResponseEntity.ok(clienteServicio.actualizarCliente(id, peticion));
    }

    /**
     * Da de baja a un cliente por su identificador UUID (baja lógica / soft delete).
     *
     * @param id Identificador UUID del cliente a dar de baja.
     * @return 204 No Content si se dio de baja con éxito.
     */
    @Operation(summary = "Dar de baja un cliente por su UUID (baja lógica)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente dado de baja exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable UUID id) {
        clienteServicio.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
