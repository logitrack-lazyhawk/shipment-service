package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.exception.TransicionEstadoInvalidaException;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.servicio.impl.EnvioServiceImpl;
import com.logitrack.shipmentservice.servicio.util.PageableSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la clase EnvioServiceImpl.
 * Se centra en la lógica de negocio relacionada con el cálculo de tarifas, gestión de estados y listados paginados.
 * Utiliza Mockito para simular el comportamiento del repositorio y verificar interacciones.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class EnvioServicioTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private PageableSanitizer pageableSanitizer;

    @InjectMocks
    private EnvioServiceImpl envioServicio;

    private Envio envioEjemplo;

    @BeforeEach
    void setUp() {
        envioEjemplo = new Envio();
        envioEjemplo.setId(UUID.randomUUID());
        envioEjemplo.setCodigoSeguimiento("LT-123456-ABCDE");
        envioEjemplo.setPeso(5.0);
        envioEjemplo.setAncho(10.0);
        envioEjemplo.setAlto(10.0);
        envioEjemplo.setLargo(20.0);
        envioEjemplo.setEstado(EstadoEnvio.PENDIENTE);
    }

    @Test
    @DisplayName("Debe calcular la tarifa correctamente según las dimensiones y el peso")
    void calcularTarifa_Exitoso() {
        Double tarifaCalculada = envioEjemplo.calcularTarifa();
        assertNotNull(tarifaCalculada);
        assertTrue(tarifaCalculada > 0);
    }

    @Test
    @DisplayName("Debe permitir cancelar el envío si está en estado PENDIENTE")
    void cancelarEnvio_EstadoPendiente_Exitoso() {
        UUID id = envioEjemplo.getId();

        when(envioRepository.findById(id)).thenReturn(Optional.of(envioEjemplo));
        when(envioRepository.save(any(Envio.class))).thenReturn(envioEjemplo);

        var respuesta = envioServicio.cancelarEnvio(id);

        assertEquals(EstadoEnvio.CANCELADO, respuesta.estado());
        verify(envioRepository, times(1)).save(envioEjemplo);
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar cancelar un envío que está EN_RUTA")
    void cancelarEnvio_EstadoEnRuta_Falla() {
        envioEjemplo.setEstado(EstadoEnvio.EN_RUTA);
        UUID id = envioEjemplo.getId();
        when(envioRepository.findById(id)).thenReturn(Optional.of(envioEjemplo));
        assertThrows(TransicionEstadoInvalidaException.class,
                () -> envioServicio.cancelarEnvio(id));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    @DisplayName("listarEnviosPaginados: Retorna página filtrada cuando se pasa estado")
    void listarEnviosPaginados_ConEstado_RetornaPaginaFiltrada() {
        Pageable pageable = PageRequest.of(0, 10);
        when(pageableSanitizer.sanitizar(pageable)).thenReturn(pageable);

        Page<Envio> paginaMock = new PageImpl<>(List.of(envioEjemplo), pageable, 1);
        when(envioRepository.findByEstado(EstadoEnvio.PENDIENTE, pageable)).thenReturn(paginaMock);

        Page<EnvioResponseDto> resultado = envioServicio.listarEnviosPaginados(EstadoEnvio.PENDIENTE, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(envioEjemplo.getId(), resultado.getContent().get(0).id());
        verify(envioRepository).findByEstado(EstadoEnvio.PENDIENTE, pageable);
    }

    @Test
    @DisplayName("listarEnviosPaginados: Retorna todos paginados cuando el estado es null")
    void listarEnviosPaginados_SinEstado_RetornaTodos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(pageableSanitizer.sanitizar(pageable)).thenReturn(pageable);

        Page<Envio> paginaMock = new PageImpl<>(List.of(envioEjemplo), pageable, 1);
        when(envioRepository.findAll(pageable)).thenReturn(paginaMock);

        Page<EnvioResponseDto> resultado = envioServicio.listarEnviosPaginados(null, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(envioRepository).findAll(pageable);
    }
}
