package com.logitrack.shipmentservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
/**
 * Decodificador de errores personalizado para manejar respuestas de error del microservicio courier-service.
 * Este decodificador intercepta los códigos de estado HTTP y lanza excepciones específicas según el caso.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */

public class CourierErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 404:
                return new IllegalArgumentException("Error: El repartidor solicitado no existe o no fue encontrado en courier-service.");
            case 500:
                return new IllegalStateException("Error Interno: El servicio de repartidores experimentó una falla crítica.");
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
