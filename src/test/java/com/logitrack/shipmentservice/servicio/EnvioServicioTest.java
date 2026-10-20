package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.servicio.impl.EnvioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * Pruebas unitarias para la clase EnvioServiceImpl.
 * Se centra en la lógica de negocio relacionada con el cálculo de tarifas y la gestión de estados de envíos.
 * Utiliza Mockito para simular el comportamiento del repositorio y verificar interacciones.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class EnvioServicioTest {

    @Mock
    private EnvioRepository envioRepository;

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
        assertThrows(com.logitrack.shipmentservice.dominio.exception.TransicionEstadoInvalidaException.class,
                () -> envioServicio.cancelarEnvio(id));
        verify(envioRepository, never()).save(any(Envio.class));
    }
}