package com.logitrack.shipmentservice.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.shipmentservice.controlador.exception.GlobalExceptionHandler;
import com.logitrack.shipmentservice.dominio.dto.ClienteRequestDto;
import com.logitrack.shipmentservice.dominio.dto.ClienteResponseDto;
import com.logitrack.shipmentservice.servicio.ClienteServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ClienteServicio clienteServicio;

    @InjectMocks
    private ClienteController clienteController;

    private UUID clienteId;
    private ClienteResponseDto responseDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(clienteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        clienteId = UUID.randomUUID();
        responseDto = new ClienteResponseDto(
                clienteId,
                "Juan Pérez",
                "30111222",
                "+541144332211",
                "juan.perez@example.com"
        );
    }

    @Test
    @DisplayName("POST /api/v1/clientes retorna 201 Created cuando la petición es válida")
    void registrarCliente_Valido_Retorna201() throws Exception {
        ClienteRequestDto requestDto = new ClienteRequestDto(
                "Juan Pérez",
                "30.111.222",
                "11-4433-2211",
                "juan.perez@example.com"
        );
        when(clienteServicio.registrarCliente(any(ClienteRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(clienteId.toString()))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.dni").value("30111222"))
                .andExpect(jsonPath("$.telefono").value("+541144332211"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"));

        verify(clienteServicio).registrarCliente(any(ClienteRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/clientes retorna 400 Bad Request cuando el nombre está vacío")
    void registrarCliente_NombreVacio_Retorna400() throws Exception {
        ClienteRequestDto requestDto = new ClienteRequestDto(
                "",
                "30111222",
                "+541144332211",
                "juan@example.com"
        );

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());

        verify(clienteServicio, never()).registrarCliente(any());
    }

    @Test
    @DisplayName("POST /api/v1/clientes retorna 400 Bad Request cuando el email es inválido")
    void registrarCliente_EmailInvalido_Retorna400() throws Exception {
        ClienteRequestDto requestDto = new ClienteRequestDto(
                "Juan Pérez",
                "30111222",
                "+541144332211",
                "correo-invalido"
        );

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        verify(clienteServicio, never()).registrarCliente(any());
    }

    @Test
    @DisplayName("GET /api/v1/clientes retorna 200 OK con página de clientes")
    void listarClientes_Retorna200() throws Exception {
        when(clienteServicio.listarClientes(any()))
                .thenReturn(new PageImpl<>(List.of(responseDto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/clientes")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(clienteId.toString()))
                .andExpect(jsonPath("$.content[0].nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.content[0].dni").value("30111222"))
                .andExpect(jsonPath("$.content[0].email").value("juan.perez@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(clienteServicio).listarClientes(any());
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{id} retorna 200 OK si el cliente existe")
    void obtenerPorId_Existe_Retorna200() throws Exception {
        when(clienteServicio.obtenerPorId(clienteId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/clientes/{id}", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId.toString()))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.dni").value("30111222"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"));

        verify(clienteServicio).obtenerPorId(clienteId);
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{id} retorna 404 Not Found si no existe")
    void obtenerPorId_NoExiste_Retorna404() throws Exception {
        when(clienteServicio.obtenerPorId(clienteId))
                .thenThrow(new IllegalArgumentException("Error: No se encontró un cliente con el ID especificado."));

        mockMvc.perform(get("/api/v1/clientes/{id}", clienteId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"));

        verify(clienteServicio).obtenerPorId(clienteId);
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{id} retorna 200 OK cuando la edición es exitosa")
    void actualizarCliente_Valido_Retorna200() throws Exception {
        ClienteRequestDto updateDto = new ClienteRequestDto(
                "Juan Pérez Actualizado",
                "30111222",
                "+541144332211",
                "juan.nuevo@example.com"
        );
        ClienteResponseDto updatedResponse = new ClienteResponseDto(
                clienteId,
                "Juan Pérez Actualizado",
                "30111222",
                "+541144332211",
                "juan.nuevo@example.com"
        );

        when(clienteServicio.actualizarCliente(eq(clienteId), any(ClienteRequestDto.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/clientes/{id}", clienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId.toString()))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez Actualizado"))
                .andExpect(jsonPath("$.email").value("juan.nuevo@example.com"));

        verify(clienteServicio).actualizarCliente(eq(clienteId), any(ClienteRequestDto.class));
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{id} retorna 404 Not Found cuando el cliente a actualizar no existe")
    void actualizarCliente_NoExiste_Retorna404() throws Exception {
        ClienteRequestDto updateDto = new ClienteRequestDto(
                "Juan Pérez",
                "30111222",
                "+541144332211",
                "juan@example.com"
        );

        when(clienteServicio.actualizarCliente(eq(clienteId), any(ClienteRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Error: No se encontró un cliente con el ID especificado."));

        mockMvc.perform(put("/api/v1/clientes/{id}", clienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(clienteServicio).actualizarCliente(eq(clienteId), any(ClienteRequestDto.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/clientes/{id} retorna 204 No Content cuando se da de baja exitosamente")
    void eliminarCliente_Existe_Retorna204() throws Exception {
        doNothing().when(clienteServicio).eliminarCliente(clienteId);

        mockMvc.perform(delete("/api/v1/clientes/{id}", clienteId))
                .andExpect(status().isNoContent());

        verify(clienteServicio).eliminarCliente(clienteId);
    }

    @Test
    @DisplayName("DELETE /api/v1/clientes/{id} retorna 404 Not Found si el cliente no existe")
    void eliminarCliente_NoExiste_Retorna404() throws Exception {
        doThrow(new IllegalArgumentException("Error: No se encontró un cliente con el ID especificado."))
                .when(clienteServicio).eliminarCliente(clienteId);

        mockMvc.perform(delete("/api/v1/clientes/{id}", clienteId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(clienteServicio).eliminarCliente(clienteId);
    }
}
