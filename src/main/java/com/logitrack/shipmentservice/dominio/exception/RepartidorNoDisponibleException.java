package com.logitrack.shipmentservice.dominio.exception;

/**
 * Excepción lanzada cuando el repartidor existe pero no está disponible o está inactivo (HTTP 400).
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public class RepartidorNoDisponibleException extends ReglaNegocioException {

    public RepartidorNoDisponibleException(String message) {
        super(message);
    }
}

