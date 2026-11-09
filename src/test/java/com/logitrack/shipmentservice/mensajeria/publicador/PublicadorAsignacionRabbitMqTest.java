
package com.logitrack.shipmentservice.mensajeria.publicador;

import com.logitrack.shipmentservice.config.AsignacionRabbitConfig;
import com.logitrack.shipmentservice.mensajeria.contrato.AsignacionSolicitadaV1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicadorAsignacionRabbitMqTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PublicadorAsignacionRabbitMq publicador;

    @Test
    void debePublicarEnExchangeCorrecto() {
        AsignacionSolicitadaV1 evento = new AsignacionSolicitadaV1(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        publicador.publicar(evento);

        verify(rabbitTemplate).convertAndSend(
                eq(AsignacionRabbitConfig.EXCHANGE),
                eq(AsignacionRabbitConfig.ROUTING_KEY),
                same(evento),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    void debeEstarConfiguradoDespuesDelCommit()
            throws NoSuchMethodException {

        var metodo = PublicadorAsignacionRabbitMq.class
                .getMethod("publicar", AsignacionSolicitadaV1.class);

        var anotacion = metodo.getAnnotation(
                TransactionalEventListener.class
        );

        assertNotNull(anotacion);
        assertEquals(
                TransactionPhase.AFTER_COMMIT,
                anotacion.phase()
        );
    }
}
