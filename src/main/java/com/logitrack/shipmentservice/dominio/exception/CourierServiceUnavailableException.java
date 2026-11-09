package com.logitrack.shipmentservice.dominio.exception;

/**
 * Excepción lanzada cuando la comunicación con courier-service falla, se produce timeout,
 * el Circuit Breaker está abierto o el servicio remoto responde con error 5xx (HTTP 503).
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public class CourierServiceUnavailableException extends RuntimeException {

    public CourierServiceUnavailableException(String message) {
        super(message);
    }

    public CourierServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

