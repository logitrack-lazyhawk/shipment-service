package com.logitrack.shipmentservice.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.shipmentservice.controlador.exception.GlobalExceptionHandler;
import com.logitrack.shipmentservice.dominio.dto.RepartidorRequestDto;
import com.logitrack.shipmentservice.dominio.dto.RepartidorResponseDto;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import com.logitrack.shipmentservice.servicio.RepartidorServicio;
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
class RepartidorControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private RepartidorServicio repartidorServicio;

    @InjectMocks
    private RepartidorController repartidorController;

    private UUID repartidorId;
    private RepartidorResponseDto responseDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(repartidorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        repartidorId = UUID.randomUUID();
        responseDto = new RepartidorResponseDto(
                repartidorId,
                "Carlos Gomez",
                "Moto",
                EstadoRepartidor.ACTIVO
        );
    }

    @Test
    @DisplayName("POST /api/v1/repartidores retorna 201 Created cuando la petición es válida")
    void registrarRepartidor_Valido_Retorna201() throws Exception {
        RepartidorRequestDto requestDto = new RepartidorRequestDto("Carlos Gomez", "Moto", EstadoRepartidor.ACTIVO);
        when(repartidorServicio.registrarRepartidor(any(RepartidorRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/repartidores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(repartidorId.toString()))
                .andExpect(jsonPath("$.nombre").value("Carlos Gomez"))
                .andExpect(jsonPath("$.tipoVehiculo").value("Moto"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        verify(repartidorServicio).registrarRepartidor(any(RepartidorRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/v1/repartidores retorna 400 Bad Request cuando el nombre está vacío")
    void registrarRepartidor_Invalido_Retorna400() throws Exception {
        RepartidorRequestDto requestDto = new RepartidorRequestDto("", "Moto", EstadoRepartidor.ACTIVO);

        mockMvc.perform(post("/api/v1/repartidores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.nombre").exists());

        verify(repartidorServicio, never()).registrarRepartidor(any());
    }

    @Test
    @DisplayName("GET /api/v1/repartidores retorna 200 OK con página de repartidores")
    void listarRepartidores_Retorna200() throws Exception {
        when(repartidorServicio.listarRepartidores(isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(responseDto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/repartidores")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(repartidorId.toString()))
                .andExpect(jsonPath("$.content[0].nombre").value("Carlos Gomez"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(repartidorServicio).listarRepartidores(isNull(), any());
    }

    @Test
    @DisplayName("GET /api/v1/repartidores?estado=ACTIVO filtra correctamente por estado")
    void listarRepartidores_ConFiltroEstado_Retorna200() throws Exception {
        when(repartidorServicio.listarRepartidores(eq(EstadoRepartidor.ACTIVO), any()))
                .thenReturn(new PageImpl<>(List.of(responseDto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/repartidores")
                        .param("estado", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].estado").value("ACTIVO"));

        verify(repartidorServicio).listarRepartidores(eq(EstadoRepartidor.ACTIVO), any());
    }

    @Test
    @DisplayName("GET /api/v1/repartidores/{id} retorna 200 OK si existe")
    void obtenerPorId_Existe_Retorna200() throws Exception {
        when(repartidorServicio.obtenerPorId(repartidorId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/repartidores/{id}", repartidorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(repartidorId.toString()))
                .andExpect(jsonPath("$.nombre").value("Carlos Gomez"));

        verify(repartidorServicio).obtenerPorId(repartidorId);
    }

    @Test
    @DisplayName("GET /api/v1/repartidores/{id} retorna 404 Not Found si no existe")
    void obtenerPorId_NoExiste_Retorna404() throws Exception {
        when(repartidorServicio.obtenerPorId(repartidorId))
                .thenThrow(new IllegalArgumentException("Error: No se encontró un repartidor con el ID especificado."));

        mockMvc.perform(get("/api/v1/repartidores/{id}", repartidorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"));

        verify(repartidorServicio).obtenerPorId(repartidorId);
    }

    @Test
    @DisplayName("DELETE /api/v1/repartidores/{id} retorna 204 No Content cuando se elimina")
    void eliminarRepartidor_Existe_Retorna204() throws Exception {
        doNothing().when(repartidorServicio).eliminarRepartidor(repartidorId);

        mockMvc.perform(delete("/api/v1/repartidores/{id}", repartidorId))
                .andExpect(status().isNoContent());

        verify(repartidorServicio).eliminarRepartidor(repartidorId);
    }

    @Test
    @DisplayName("DELETE /api/v1/repartidores/{id} retorna 404 Not Found si el repartidor a eliminar no existe")
    void eliminarRepartidor_NoExiste_Retorna404() throws Exception {
        doThrow(new IllegalArgumentException("Error: No se encontró un repartidor con el ID especificado."))
                .when(repartidorServicio).eliminarRepartidor(repartidorId);

        mockMvc.perform(delete("/api/v1/repartidores/{id}", repartidorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(repartidorServicio).eliminarRepartidor(repartidorId);
    }
}
