package com.logitrack.shipmentservice.dominio.exception;

/**
 * Excepción lanzada cuando el repartidor solicitado no existe en courier-service (HTTP 404).
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public class RepartidorNoEncontradoException extends RuntimeException {

    public RepartidorNoEncontradoException(String message) {
        super(message);
    }

    public RepartidorNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}

