package com.logitrack.shipmentservice.config;

import com.logitrack.shipmentservice.cliente.courier.CourierClient;
import com.logitrack.shipmentservice.dominio.exception.CourierServiceUnavailableException;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoDisponibleException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoEncontradoException;
import com.logitrack.shipmentservice.servicio.validador.RepartidorValidator;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Pruebas unitarias para la fábrica de contingencia CourierClientFallback.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 2.0.0
 */
class CourierClientFallbackTest {

    private final CourierClientFallback factory = new CourierClientFallback();

    @Test
    @DisplayName("Timeout se propaga como CourierServiceUnavailableException")
    void timeoutSePropagaComoIndisponibilidad() {
        var cause = new TimeoutException("Courier lento");
        var validator = new RepartidorValidator(factory.create(cause));
        var error = assertThrows(CourierServiceUnavailableException.class,
                () -> validator.validarDisponibilidadRemota(UUID.randomUUID()));
        assertSame(cause, error.getCause());
    }

    @Test
    @DisplayName("Conserva excepción de repartidor no encontrado del decoder")
    void conservaRepartidorInexistenteDelDecoder() {
        var cause = new RepartidorNoEncontradoException("Repartidor inexistente");
        var validator = new RepartidorValidator(factory.create(cause));
        assertSame(cause, assertThrows(RepartidorNoEncontradoException.class,
                () -> validator.validarDisponibilidadRemota(UUID.randomUUID())));
    }

    @Test
    @DisplayName("Conserva BadRequest como RepartidorNoDisponibleException")
    void conservaBadRequestComoReglaDeNegocio() {
        var validator = new RepartidorValidator(factory.create(mock(FeignException.BadRequest.class)));
        assertThrows(RepartidorNoDisponibleException.class,
                () -> validator.validarDisponibilidadRemota(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Debe lanzar CourierServiceUnavailableException con mensaje controlado cuando se activa el Fallback")
    void testConsultarDisponibilidadMensajeFallback() {
        var causaCaida = new RuntimeException("Servicio apagado");
        CourierClient clienteContingencia = factory.create(causaCaida);
        Exception excepcionLanzada = assertThrows(CourierServiceUnavailableException.class, () ->
                clienteContingencia.consultarDisponibilidad(UUID.randomUUID())
        );
        String mensajeEsperado = "Servicio no disponible: No se pudo conectar con courier-service o el tiempo de espera se agotó.";
        assertEquals(mensajeEsperado, excepcionLanzada.getMessage(), "El mensaje del Fallback no coincide con lo esperado.");
    }
}
