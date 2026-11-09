
package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.SolicitudAsignacionResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.entidad.SolicitudAsignacion;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoSolicitudAsignacion;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.repositorio.SolicitudAsignacionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/*
    Servicio que maneja la lógica de negocio relacionada con las solicitudes de asignación.
    Proporciona métodos para solicitar asignaciones y publicar eventos relacionados.
    @author: Equipo de LazyHawks
    @version: 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SolicitudAsignacionService {

    private final EnvioRepository envioRepository;
    private final SolicitudAsignacionRepository solicitudRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SolicitudAsignacionResponseDto solicitarAsignacion(
            UUID envioId,
            UUID repartidorId
    ) {
        if (repartidorId == null) {
            throw new ReglaNegocioException(
                    "El identificador del repartidor es obligatorio."
            );
        }

        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el envío solicitado."
                ));

        if (envio.getEstado() != EstadoEnvio.PENDIENTE) {
            throw new ReglaNegocioException(
                    "Solo se pueden solicitar asignaciones " +
                            "para envíos PENDIENTES."
            );
        }

        if (envio.getRepartidorId() != null) {
            throw new ReglaNegocioException(
                    "El envío ya tiene un repartidor asignado."
            );
        }

        boolean existePendiente =
                solicitudRepository.existsByEnvioIdAndEstado(
                        envioId,
                        EstadoSolicitudAsignacion.PENDIENTE_CONFIRMACION
                );

        if (existePendiente) {
            throw new ReglaNegocioException(
                    "El envío ya tiene una solicitud pendiente."
            );
        }

        UUID solicitudId = UUID.randomUUID();
        Instant ahora = Instant.now();

        SolicitudAsignacion solicitud = new SolicitudAsignacion(
                solicitudId,
                envioId,
                repartidorId,
                EstadoSolicitudAsignacion.PENDIENTE_CONFIRMACION,
                ahora
        );

        solicitudRepository.saveAndFlush(solicitud);

        AsignacionSolicitadaV1 evento = new AsignacionSolicitadaV1(
                UUID.randomUUID(),
                solicitudId,
                1,
                ahora,
                envioId,
                repartidorId
        );

        eventPublisher.publishEvent(evento);

        return new SolicitudAsignacionResponseDto(
                solicitudId,
                envioId,
                repartidorId,
                EstadoSolicitudAsignacion.PENDIENTE_CONFIRMACION
        );
    }
}
