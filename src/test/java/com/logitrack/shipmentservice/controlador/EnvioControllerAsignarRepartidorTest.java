package com.logitrack.shipmentservice.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.shipmentservice.controlador.exception.GlobalExceptionHandler;
import com.logitrack.shipmentservice.dominio.dto.AsignarRepartidorRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.servicio.EnvioServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EnvioControllerAsignarRepartidorTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private EnvioServicio envioServicio;

    @InjectMocks
    private EnvioController envioController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(envioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/envios/{id}/asignar-repartidor retorna 200 cuando es exitoso")
    void asignarRepartidor_Retorna200() throws Exception {
        UUID envioId = UUID.randomUUID();
        UUID repartidorId = UUID.randomUUID();

        AsignarRepartidorRequestDto requestDto = new AsignarRepartidorRequestDto(repartidorId);
        EnvioResponseDto responseDto = new EnvioResponseDto(
                envioId, "LT-123456-ABCDE", 3.0, 10.0, 10.0, 10.0, 20.0,
                EstadoEnvio.PENDIENTE, null, null, repartidorId
        );

        when(envioServicio.asignarRepartidor(envioId, repartidorId)).thenReturn(responseDto);

        mockMvc.perform(post("/api/envios/{id}/asignar-repartidor", envioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(envioId.toString()))
                .andExpect(jsonPath("$.repartidorId").value(repartidorId.toString()));
    }

    @Test
    @DisplayName("POST /api/envios/{id}/asignar-repartidor retorna 400 cuando viola regla de negocio")
    void asignarRepartidor_Retorna400_ReglaNegocio() throws Exception {
        UUID envioId = UUID.randomUUID();
        UUID repartidorId = UUID.randomUUID();

        AsignarRepartidorRequestDto requestDto = new AsignarRepartidorRequestDto(repartidorId);

        when(envioServicio.asignarRepartidor(envioId, repartidorId))
                .thenThrow(new ReglaNegocioException("Operación inválida: No se puede asignar un repartidor a un envío en estado EN_RUTA."));

        mockMvc.perform(post("/api/envios/{id}/asignar-repartidor", envioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Violación de Regla de Negocio"))
                .andExpect(jsonPath("$.message").value("Operación inválida: No se puede asignar un repartidor a un envío en estado EN_RUTA."));
    }

    @Test
    @DisplayName("POST /api/envios/{id}/asignar-repartidor retorna 404 cuando el recurso no existe")
    void asignarRepartidor_Retorna404_NoExiste() throws Exception {
        UUID envioId = UUID.randomUUID();
        UUID repartidorId = UUID.randomUUID();

        AsignarRepartidorRequestDto requestDto = new AsignarRepartidorRequestDto(repartidorId);

        when(envioServicio.asignarRepartidor(envioId, repartidorId))
                .thenThrow(new IllegalArgumentException("Error: No se encontró un envío con el ID especificado."));

        mockMvc.perform(post("/api/envios/{id}/asignar-repartidor", envioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"));
    }
}
