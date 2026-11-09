
package com.logitrack.shipmentservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
    Configuración de RabbitMQ para la publicación de eventos de asignación.
    Define el exchange y la clave de enrutamiento para los eventos relacionados con las solicitudes de asignación.
    @author: Equipo de LazyHawks
    @version: 1.0.0
 */
@Configuration
public class AsignacionRabbitConfig {

    public static final String EXCHANGE =
            "logitrack.assignment.events";

    public static final String ROUTING_KEY =
            "shipment.assignment.requested.v1";

    @Bean
    public TopicExchange exchangeAsignaciones() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
