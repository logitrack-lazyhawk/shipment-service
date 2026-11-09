
package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.entidad.SolicitudAsignacion;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoSolicitudAsignacion;
import com.logitrack.shipmentservice.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.repositorio.SolicitudAsignacionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudAsignacionResultadoTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private SolicitudAsignacionRepository solicitudRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SolicitudAsignacionService servicio;

    private final UUID solicitudId = UUID.randomUUID();
    private final UUID envioId = UUID.randomUUID();
    private final UUID repartidorId = UUID.randomUUID();

    private SolicitudAsignacion crearSolicitud() {
        return new SolicitudAsignacion(
                solicitudId,
                envioId,
                repartidorId,
                EstadoSolicitudAsignacion.PENDIENTE_CONFIRMACION,
                Instant.now()
        );
    }

    private AsignacionResultadoV1 crearResultado(boolean aceptada) {
        return new AsignacionResultadoV1(
                UUID.randomUUID(),
                solicitudId,
                1,
                Instant.now(),
                envioId,
                repartidorId,
                aceptada,
                aceptada ? null : "Repartidor no disponible"
        );
    }

    @Test
    void debeConfirmarAsignacion() {
        SolicitudAsignacion solicitud = crearSolicitud();
        Envio envio = Envio.builder()
                .estado(EstadoEnvio.PENDIENTE)
                .build();

        when(solicitudRepository.findById(solicitudId))
                .thenReturn(Optional.of(solicitud));

        when(envioRepository.findById(envioId))
                .thenReturn(Optional.of(envio));

        servicio.procesarResultado(crearResultado(true));

        assertEquals(
                EstadoSolicitudAsignacion.CONFIRMADA,
                solicitud.getEstado()
        );
        assertEquals(repartidorId, envio.getRepartidorId());
        assertEquals(EstadoEnvio.PENDIENTE, envio.getEstado());

        verify(envioRepository).save(envio);
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void debeRechazarAsignacion() {
        SolicitudAsignacion solicitud = crearSolicitud();

        when(solicitudRepository.findById(solicitudId))
                .thenReturn(Optional.of(solicitud));

        servicio.procesarResultado(crearResultado(false));

        assertEquals(
                EstadoSolicitudAsignacion.RECHAZADA,
                solicitud.getEstado()
        );

        verifyNoInteractions(envioRepository);
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void noDebeProcesarDosVeces() {
        SolicitudAsignacion solicitud = crearSolicitud();
        solicitud.setEstado(EstadoSolicitudAsignacion.CONFIRMADA);

        when(solicitudRepository.findById(solicitudId))
                .thenReturn(Optional.of(solicitud));

        servicio.procesarResultado(crearResultado(true));

        verifyNoInteractions(envioRepository);
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void debeRechazarVersionInvalida() {
        AsignacionResultadoV1 evento = new AsignacionResultadoV1(
                UUID.randomUUID(),
                solicitudId,
                2,
                Instant.now(),
                envioId,
                repartidorId,
                true,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.procesarResultado(evento)
        );

        verifyNoInteractions(
                solicitudRepository,
                envioRepository
        );
    }
}
