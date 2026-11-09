
package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.entidad.SolicitudAsignacion;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoSolicitudAsignacion;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.repositorio.SolicitudAsignacionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
class SolicitudAsignacionServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private SolicitudAsignacionRepository solicitudRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SolicitudAsignacionService servicio;

    @Test
    void debePersistirYEmitirEvento() {
        UUID envioId = UUID.randomUUID();
        UUID repartidorId = UUID.randomUUID();

        Envio envio = Envio.builder()
                .estado(EstadoEnvio.PENDIENTE)
                .build();

        envio.setId(envioId);

        when(envioRepository.findById(envioId))
                .thenReturn(Optional.of(envio));

        when(solicitudRepository.saveAndFlush(
                any(SolicitudAsignacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = servicio.solicitarAsignacion(
                envioId,
                repartidorId
        );

        assertNotNull(respuesta.solicitudId());

        assertEquals(
                EstadoSolicitudAsignacion.PENDIENTE_CONFIRMACION,
                respuesta.estado()
        );

        ArgumentCaptor<AsignacionSolicitadaV1> captor =
                ArgumentCaptor.forClass(AsignacionSolicitadaV1.class);

        InOrder orden = inOrder(
                solicitudRepository,
                eventPublisher
        );

        orden.verify(solicitudRepository)
                .saveAndFlush(any(SolicitudAsignacion.class));

        orden.verify(eventPublisher)
                .publishEvent(captor.capture());

        AsignacionSolicitadaV1 evento = captor.getValue();

        assertEquals(1, evento.version());
        assertEquals(envioId, evento.envioId());
        assertEquals(repartidorId, evento.repartidorId());
        assertEquals(respuesta.solicitudId(), evento.solicitudId());
    }

    @Test
    void noDebePublicarSiEstadoEsInvalido() {
        UUID envioId = UUID.randomUUID();
        UUID repartidorId = UUID.randomUUID();

        Envio envio = Envio.builder()
                .estado(EstadoEnvio.EN_RUTA)
                .build();

        when(envioRepository.findById(envioId))
                .thenReturn(Optional.of(envio));

        assertThrows(
                ReglaNegocioException.class,
                () -> servicio.solicitarAsignacion(
                        envioId,
                        repartidorId
                )
        );

        verifyNoInteractions(
                solicitudRepository,
                eventPublisher
        );
    }
}
