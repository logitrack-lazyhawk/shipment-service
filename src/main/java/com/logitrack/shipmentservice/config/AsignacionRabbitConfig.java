
package com.logitrack.shipmentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
    Configuración de RabbitMQ para la asignación de envíos.
    Define el exchange, colas y bindings necesarios para la comunicación.
    @author Equipo de LazyHawks
    @version 1.0.0
 */
@Configuration
public class AsignacionRabbitConfig {

    public static final String EXCHANGE =
            "logitrack.assignment.events";

    public static final String ROUTING_KEY =
            "shipment.assignment.requested.v1";

    public static final String COLA_RESULTADOS =
            "shipment.assignment.results.q";

    public static final String RK_RESULTADOS =
            "courier.assignment.*.v1";

    @Bean
    public TopicExchange exchangeAsignaciones() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue colaResultados() {
        return QueueBuilder.durable(COLA_RESULTADOS).build();
    }

    @Bean
    public Binding bindingResultados(
            @Qualifier("colaResultados") Queue cola,
            TopicExchange exchange) {

        return BindingBuilder.bind(cola)
                .to(exchange)
                .with(RK_RESULTADOS);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
