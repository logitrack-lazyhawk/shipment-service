package com.logitrack.shipmentservice.config;

import com.logitrack.shipmentservice.dominio.exception.CourierServiceUnavailableException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoDisponibleException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoEncontradoException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para CourierErrorDecoder.
 * Verifica la decodificación desacoplada de errores HTTP 4xx y 5xx en excepciones tipadas de negocio.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 2.0.0
 */
class CourierErrorDecoderTest {

    private final CourierErrorDecoder decoder = new CourierErrorDecoder();

    private Response mockFeignResponse(int status) {
        return Response.builder()
                .status(status)
                .reason("Error simulado para pruebas")
                .request(Request.create(Request.HttpMethod.GET, "/api/v1/repartidores/test", Collections.emptyMap(), null, Charset.defaultCharset(), null))
                .headers(new HashMap<>())
                .build();
    }

    @Test
    @DisplayName("Debe lanzar RepartidorNoEncontradoException al recibir HTTP 404 de courier-service")
    void testError404() {
        Response response = mockFeignResponse(404);
        Exception ex = decoder.decode("CourierClient#consultarDisponibilidad", response);

        assertInstanceOf(RepartidorNoEncontradoException.class, ex, "Se esperaba RepartidorNoEncontradoException para error 404");
        assertTrue(ex.getMessage().contains("no existe o no fue encontrado"));
    }

    @Test
    @DisplayName("Debe lanzar RepartidorNoDisponibleException al recibir HTTP 400 de courier-service")
    void testError400() {
        Response response = mockFeignResponse(400);
        Exception ex = decoder.decode("CourierClient#consultarDisponibilidad", response);

        assertInstanceOf(RepartidorNoDisponibleException.class, ex, "Se esperaba RepartidorNoDisponibleException para error 400");
        assertTrue(ex.getMessage().contains("inactivo y no está disponible"));
    }

    @Test
    @DisplayName("Debe lanzar RepartidorNoDisponibleException al recibir HTTP 409 de courier-service")
    void testError409() {
        Response response = mockFeignResponse(409);
        Exception ex = decoder.decode("CourierClient#consultarDisponibilidad", response);

        assertInstanceOf(RepartidorNoDisponibleException.class, ex, "Se esperaba RepartidorNoDisponibleException para error 409");
        assertTrue(ex.getMessage().contains("asignado"));
    }

    @Test
    @DisplayName("Debe lanzar CourierServiceUnavailableException al recibir HTTP 500 de courier-service")
    void testError500() {
        Response response = mockFeignResponse(500);
        Exception ex = decoder.decode("CourierClient#consultarDisponibilidad", response);

        assertInstanceOf(CourierServiceUnavailableException.class, ex, "Se esperaba CourierServiceUnavailableException para error 500");
        assertTrue(ex.getMessage().contains("falla crítica"));
    }

    @Test
    @DisplayName("Debe lanzar CourierServiceUnavailableException al recibir HTTP 503 de courier-service")
    void testError503() {
        Response response = mockFeignResponse(503);
        Exception ex = decoder.decode("CourierClient#consultarDisponibilidad", response);

        assertInstanceOf(CourierServiceUnavailableException.class, ex, "Se esperaba CourierServiceUnavailableException para error 503");
        assertTrue(ex.getMessage().contains("no responde"));
    }
}
