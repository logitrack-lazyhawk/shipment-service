
package com.logitrack.shipmentservice.mensajeria.publicador;

import com.logitrack.shipmentservice.config.AsignacionRabbitConfig;
import com.logitrack.shipmentservice.mensajeria.contrato.AsignacionSolicitadaV1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
    Componente que publica eventos de solicitud de asignación en RabbitMQ.
    Escucha eventos de tipo AsignacionSolicitadaV1 y los envía al exchange y routing key configurados.
    @author: Equipo de LazyHawks
    @version: 1.0.0
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicadorAsignacionRabbitMq {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void publicar(AsignacionSolicitadaV1 evento) {

        try {
            rabbitTemplate.convertAndSend(
                    AsignacionRabbitConfig.EXCHANGE,
                    AsignacionRabbitConfig.ROUTING_KEY,
                    evento,
                    mensaje -> {
                        mensaje.getMessageProperties().setMessageId(
                                evento.eventoId().toString()
                        );

                        mensaje.getMessageProperties().setDeliveryMode(
                                MessageDeliveryMode.PERSISTENT
                        );

                        return mensaje;
                    }
            );

            log.info(
                    "Solicitud de asignación enviada. " +
                            "solicitudId={}, envioId={}, repartidorId={}",
                    evento.solicitudId(),
                    evento.envioId(),
                    evento.repartidorId()
            );

        } catch (AmqpException ex) {
            log.error(
                    "No se pudo publicar la solicitud {} en RabbitMQ",
                    evento.solicitudId(),
                    ex
            );
        }
    }
}
