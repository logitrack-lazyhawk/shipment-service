package com.logitrack.shipmentservice.config;

import com.logitrack.shipmentservice.cliente.courier.CourierClient;
import com.logitrack.shipmentservice.servicio.validador.RepartidorValidator;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CourierClientFallbackTest {
    private final CourierClientFallback factory = new CourierClientFallback();

    @Test
    void timeoutSePropagaComoIndisponibilidad() {
        var cause = new TimeoutException("Courier lento");
        var validator = new RepartidorValidator(factory.create(cause));
        var error = assertThrows(IllegalStateException.class,
                () -> validator.validarDisponibilidadRemota(UUID.randomUUID()));
        assertSame(cause, error.getCause());
    }

    @Test
    void conservaRepartidorInexistenteDelDecoder() {
        var cause = new IllegalArgumentException("Repartidor inexistente");
        var validator = new RepartidorValidator(factory.create(cause));
        assertSame(cause, assertThrows(IllegalArgumentException.class,
                () -> validator.validarDisponibilidadRemota(UUID.randomUUID())));
    }

    @Test
    void conservaBadRequestComoReglaDeNegocio() {
        var validator = new RepartidorValidator(factory.create(mock(FeignException.BadRequest.class)));
        assertThrows(ReglaNegocioException.class,
                () -> validator.validarDisponibilidadRemota(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException con mensaje controlado cuando se activa el Fallback")
    void testConsultarDisponibilidadMensajeFallback() {
        var causaCaida = new RuntimeException("Servicio apagado");
        CourierClient clienteContingencia = factory.create(causaCaida);
        Exception excepcionLanzada = assertThrows(IllegalStateException.class, () -> {
            clienteContingencia.consultarDisponibilidad(UUID.randomUUID());
        });
        String mensajeEsperado = "Servicio no disponible: No se pudo conectar con courier-service o el tiempo de espera se agotó.";
        assertEquals(mensajeEsperado, excepcionLanzada.getMessage(), "El mensaje del Fallback no coincide con lo esperado.");
    }
}
