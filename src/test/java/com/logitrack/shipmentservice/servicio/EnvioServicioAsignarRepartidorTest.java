package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.entidad.Repartidor;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.repositorio.HistorialEnvioRepository;
import com.logitrack.shipmentservice.servicio.impl.EnvioServiceImpl;
import com.logitrack.shipmentservice.servicio.validador.RepartidorValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServicioAsignarRepartidorTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private RepartidorValidator repartidorValidator;

    @Mock
    private HistorialEnvioRepository historialRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EnvioServiceImpl envioServicio;

    private UUID envioId;
    private UUID repartidorId;
    private Envio envio;
    private Repartidor repartidor;

    @BeforeEach
    void setUp() {
        envioId = UUID.randomUUID();
        repartidorId = UUID.randomUUID();

        envio = Envio.builder()
                .codigoSeguimiento("LT-123456-ABCDE")
                .peso(3.0)
                .ancho(10.0)
                .alto(10.0)
                .largo(10.0)
                .tarifa(20.0)
                .estado(EstadoEnvio.PENDIENTE)
                .build();
        envio.setId(envioId);

        repartidor = Repartidor.builder()
                .nombre("Juan Perez")
                .dni("32333444")
                .telefono("+541166554433")
                .tipoVehiculo("Moto")
                .estado(EstadoRepartidor.ACTIVO)
                .build();
        repartidor.setId(repartidorId);
    }

    @Test
    @DisplayName("Debe asignar exitosamente un repartidor ACTIVO a un envío PENDIENTE")
    void asignarRepartidor_Exitoso() {
        when(envioRepository.findById(envioId)).thenReturn(Optional.of(envio));
        when(repartidorValidator.validarYObtenerPorId(repartidorId)).thenReturn(repartidor);
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EnvioResponseDto respuesta = envioServicio.asignarRepartidor(envioId, repartidorId);

        assertNotNull(respuesta);
        assertEquals(envioId, respuesta.id());
        assertEquals(repartidorId, respuesta.repartidorId());
        assertEquals(repartidor, envio.getRepartidor());
        verify(envioRepository).save(envio);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el envío no se encuentra en estado PENDIENTE")
    void asignarRepartidor_Falla_EstadoNoPendiente() {
        envio.setEstado(EstadoEnvio.EN_RUTA);
        when(envioRepository.findById(envioId)).thenReturn(Optional.of(envio));
        when(repartidorValidator.validarYObtenerPorId(repartidorId)).thenReturn(repartidor);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                envioServicio.asignarRepartidor(envioId, repartidorId)
        );

        assertTrue(ex.getMessage().contains("Debe encontrarse en estado PENDIENTE"));
        verify(envioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el repartidor se encuentra INACTIVO")
    void asignarRepartidor_Falla_RepartidorInactivo() {
        repartidor.setEstado(EstadoRepartidor.INACTIVO);
        when(envioRepository.findById(envioId)).thenReturn(Optional.of(envio));
        when(repartidorValidator.validarYObtenerPorId(repartidorId)).thenReturn(repartidor);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                envioServicio.asignarRepartidor(envioId, repartidorId)
        );

        assertTrue(ex.getMessage().contains("ACTIVO"));
        verify(envioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el envío no existe")
    void asignarRepartidor_Falla_EnvioNoExiste() {
        when(envioRepository.findById(envioId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                envioServicio.asignarRepartidor(envioId, repartidorId)
        );

        verify(repartidorValidator, never()).validarYObtenerPorId(any());
        verify(envioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe propagar excepción si el repartidor no existe")
    void asignarRepartidor_Falla_RepartidorNoExiste() {
        when(envioRepository.findById(envioId)).thenReturn(Optional.of(envio));
        when(repartidorValidator.validarYObtenerPorId(repartidorId))
                .thenThrow(new IllegalArgumentException("Error: No se encontró un repartidor con el ID especificado."));

        assertThrows(IllegalArgumentException.class, () ->
                envioServicio.asignarRepartidor(envioId, repartidorId)
        );

        verify(envioRepository, never()).save(any());
    }
}
