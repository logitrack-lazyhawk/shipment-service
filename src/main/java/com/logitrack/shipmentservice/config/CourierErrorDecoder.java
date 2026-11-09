package com.logitrack.shipmentservice.config;

import com.logitrack.shipmentservice.dominio.exception.CourierServiceUnavailableException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoDisponibleException;
import com.logitrack.shipmentservice.dominio.exception.RepartidorNoEncontradoException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Decodificador de errores personalizado para la comunicación síncrona con courier-service.
 * Refactoriza la interceptación de códigos HTTP separando las responsabilidades de
 * errores de cliente (4xx) y fallos de servicio/red (5xx) en excepciones tipadas de dominio.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 2.0.0
 */
@Slf4j
public class CourierErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        log.warn("Interceptado error HTTP {} desde courier-service en método '{}'", status, methodKey);

        if (status >= 400 && status < 500) {
            return handleClientError(status, methodKey, response);
        }

        if (status >= 500 && status < 600) {
            return handleServerError(status, methodKey, response);
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }

    /**
     * Gestiona errores de cliente (4xx), devolviendo excepciones tipadas de negocio.
     */
    private Exception handleClientError(int status, String methodKey, Response response) {
        return switch (status) {
            case 404 -> new RepartidorNoEncontradoException(
                    "Error: El repartidor solicitado no existe o no fue encontrado en courier-service.");
            case 400 -> new RepartidorNoDisponibleException(
                    "Operación no permitida: El repartidor se encuentra inactivo y no está disponible.");
            case 409 -> new RepartidorNoDisponibleException(
                    "Conflicto: El repartidor seleccionado ya se encuentra asignado a otra entrega.");
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }

    /**
     * Gestiona errores de servidor e indisponibilidad (5xx), traduciéndolos a fallos de infraestructura tipados.
     */
    private Exception handleServerError(int status, String methodKey, Response response) {
        return switch (status) {
            case 500 -> new CourierServiceUnavailableException(
                    "Servicio no disponible: El servicio de repartidores experimentó una falla crítica.");
            case 502, 503, 504 -> new CourierServiceUnavailableException(
                    "Servicio no disponible: courier-service no responde o el tiempo de espera se agotó.");
            default -> new CourierServiceUnavailableException(
                    "Falla en courier-service con código HTTP: " + status);
        };
    }
}
