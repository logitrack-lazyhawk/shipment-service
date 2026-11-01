package com.logitrack.shipmentservice.config;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para la clase CourierErrorDecoder.
 * Verifica que los códigos de error HTTP devuelvan las excepciones esperadas.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */

class CourierErrorDecoderTest {

    private final CourierErrorDecoder decoder = new CourierErrorDecoder();

    private Response mockFeignResponse(int status) {
        return Response.builder()
                .status(status)
                .reason("Error simulado para pruebas")
                .request(Request.create(Request.HttpMethod.GET, "/api", Collections.emptyMap(), null, Charset.defaultCharset(), null))
                .headers(new HashMap<>())
                .build();
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException al recibir HTTP 404 de courier-service")
    void testError404() {
        Response response = mockFeignResponse(404);
        Exception ex = decoder.decode("CourierClient#consultarDisponibilidad", response);

        assertTrue(ex instanceof IllegalArgumentException, "Se esperaba IllegalArgumentException para error 404");
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException al recibir HTTP 500 de courier-service")
    void testError500() {
        Response response = mockFeignResponse(500);
        Exception ex = decoder.decode("CourierClient#consultarDisponibilidad", response);

        assertTrue(ex instanceof IllegalStateException, "Se esperaba IllegalStateException para error 500");
    }

    @Test
    @DisplayName("Debe delegar al decodificador por defecto al recibir HTTP 400")
    void testError400() {
        Response response = mockFeignResponse(400);
        Exception ex = decoder.decode("CourierClient#consultarDisponibilidad", response);

        assertTrue(ex.getClass().getName().contains("FeignException"), "Se esperaba una excepción nativa de Feign para error 400");
    }
}