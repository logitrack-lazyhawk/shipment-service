package com.logitrack.shipmentservice.dominio.exception;

/**
 * Excepción lanzada cuando una operación viola una regla de negocio del dominio.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
