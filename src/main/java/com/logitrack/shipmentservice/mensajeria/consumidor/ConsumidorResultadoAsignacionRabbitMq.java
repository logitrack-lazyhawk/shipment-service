
package com.logitrack.shipmentservice.mensajeria.consumidor;

import com.logitrack.shipmentservice.config.AsignacionRabbitConfig;
import com.logitrack.shipmentservice.mensajeria.contrato.AsignacionResultadoV1;
import com.logitrack.shipmentservice.servicio.SolicitudAsignacionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumidorResultadoAsignacionRabbitMq {

    private final SolicitudAsignacionService solicitudService;

    @RabbitListener(queues = AsignacionRabbitConfig.COLA_RESULTADOS)
    public void consumir(AsignacionResultadoV1 resultado) {

        try {
            solicitudService.procesarResultado(resultado);

            log.info(
                    "Resultado procesado para solicitud {}",
                    resultado.solicitudId()
            );

        } catch (IllegalArgumentException ex) {

            log.error(
                    "Resultado de asignación inválido: {}",
                    ex.getMessage()
            );

            throw new AmqpRejectAndDontRequeueException(
                    "Resultado de asignación inválido.",
                    ex
            );
        }
    }
}
