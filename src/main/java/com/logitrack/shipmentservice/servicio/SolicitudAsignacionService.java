
package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.SolicitudAsignacionResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.entidad.SolicitudAsignacion;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoSolicitudAsignacion;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.shipmentservice.mensajeria.contrato.AsignacionSolicitadaV1;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.repositorio.SolicitudAsignacionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
/**
 * Servicio para gestionar solicitudes de asignación de envíos.
 * Permite solicitar asignaciones y procesar resultados de asignación.
 * @version 1.0.0
 * @author Equipo de LazyHawks
 */
@Slf4j
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
                    "Solo se pueden solicitar asignaciones para envíos PENDIENTES."
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

    @Transactional
    public void procesarResultado(AsignacionResultadoV1 resultado) {

        validarResultado(resultado);

        SolicitudAsignacion solicitud = solicitudRepository
                .findById(resultado.solicitudId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La solicitud de asignación no existe."
                ));

        if (!solicitud.getEnvioId().equals(resultado.envioId())
                || !solicitud.getRepartidorId().equals(resultado.repartidorId())) {
            throw new IllegalArgumentException(
                    "Los identificadores del resultado no coinciden con la solicitud."
            );
        }

        if (solicitud.getEstado()
                != EstadoSolicitudAsignacion.PENDIENTE_CONFIRMACION) {

            boolean mismoResultado =
                    (solicitud.getEstado() == EstadoSolicitudAsignacion.CONFIRMADA
                            && resultado.aceptada())
                            || (solicitud.getEstado() == EstadoSolicitudAsignacion.RECHAZADA
                            && !resultado.aceptada());

            if (mismoResultado) {
                log.info(
                        "Solicitud {} ya resuelta. Se ignora el resultado repetido.",
                        solicitud.getId()
                );
                return;
            }

            throw new IllegalArgumentException(
                    "La solicitud ya fue resuelta con un resultado diferente."
            );
        }

        if (resultado.aceptada()) {

            Envio envio = envioRepository.findById(resultado.envioId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe el envío asociado a la solicitud."
                    ));

            if (envio.getEstado() != EstadoEnvio.PENDIENTE
                    || envio.getRepartidorId() != null) {

                solicitud.setEstado(
                        EstadoSolicitudAsignacion.RECHAZADA
                );

                solicitudRepository.save(solicitud);

                log.warn(
                        "Solicitud {} rechazada localmente: envío no disponible.",
                        solicitud.getId()
                );
                return;
            }

            envio.asignarRepartidor(resultado.repartidorId());
            envioRepository.save(envio);

            solicitud.setEstado(
                    EstadoSolicitudAsignacion.CONFIRMADA
            );

            log.info(
                    "Asignación confirmada. Envio={}, repartidor={}",
                    envio.getId(),
                    resultado.repartidorId()
            );

        } else {

            solicitud.setEstado(
                    EstadoSolicitudAsignacion.RECHAZADA
            );

            log.warn(
                    "Asignación rechazada. Solicitud={}, motivo={}",
                    solicitud.getId(),
                    resultado.motivo()
            );
        }

        solicitudRepository.save(solicitud);
    }

    private void validarResultado(AsignacionResultadoV1 resultado) {

        if (resultado == null
                || resultado.eventoId() == null
                || resultado.solicitudId() == null
                || resultado.version() != 1
                || resultado.ocurridoEn() == null
                || resultado.envioId() == null
                || resultado.repartidorId() == null) {

            throw new IllegalArgumentException(
                    "Evento de resultado inválido o incompatible."
            );
        }
    }
}
