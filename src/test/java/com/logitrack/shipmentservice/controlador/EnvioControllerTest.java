package com.logitrack.shipmentservice.controlador;

import com.logitrack.shipmentservice.dominio.dto.EnvioRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.servicio.EnvioServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvioControllerTest {

    @Mock
    private EnvioServicio envioService;

    @InjectMocks
    private EnvioController envioController;

    @Test
    @DisplayName("Debe recibir la petición y retornar 201 CREATED al registrar un envío")
    void registrarEnvio_Exitoso() {
        EnvioRequestDto peticionMock = null;
        EnvioResponseDto respuestaMock = null;
        when(envioService.registrarEnvio(any())).thenReturn(respuestaMock);
        ResponseEntity<EnvioResponseDto> resultado = envioController.registrarEnvio(peticionMock);
        assertNotNull(resultado);
        assertEquals(HttpStatus.CREATED, resultado.getStatusCode());
        verify(envioService, times(1)).registrarEnvio(any());
    }

    @Test
    @DisplayName("Debe retornar 200 OK con página de envíos")
    void listarEnviosPaginados_Exitoso() {
        Page<EnvioResponseDto> paginaMock = Page.empty();
        when(envioService.listarEnviosPaginados(any(), any())).thenReturn(paginaMock);

        ResponseEntity<Page<EnvioResponseDto>> respuesta = envioController.listarEnviosPaginados(null, PageRequest.of(0, 10));

        assertNotNull(respuesta);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        verify(envioService, times(1)).listarEnviosPaginados(any(), any());
    }
}
