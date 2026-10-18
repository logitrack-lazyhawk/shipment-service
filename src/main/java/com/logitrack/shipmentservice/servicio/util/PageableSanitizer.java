package com.logitrack.shipmentservice.servicio.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Componente utilitario para normalizar y sanitizar objetos Pageable recibidos desde la capa web.
 * Previene errores de parseo cuando se envían espacios (ej: 'sort=nombre, desc') o direcciones aisladas (ej: 'sort=asc').
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Component
public class PageableSanitizer {

    /**
     * Sanitiza el objeto Pageable corrigiendo las órdenes de clasificación.
     *
     * @param pageable Objeto Pageable a sanitizar.
     * @return Una instancia de Pageable con las órdenes corregidas o sin orden si eran inválidas.
     */
    public Pageable sanitizar(Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return pageable;
        }

        List<Sort.Order> resultado = new ArrayList<>();
        Sort.Order ordenAnterior = null;

        for (Sort.Order order : pageable.getSort()) {
            String prop = order.getProperty() != null ? order.getProperty().trim() : "";
            if (prop.isEmpty()) {
                continue;
            }

            if (prop.equalsIgnoreCase("asc") || prop.equalsIgnoreCase("desc")) {
                if (ordenAnterior != null) {
                    Sort.Direction dir = prop.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                    resultado.remove(resultado.size() - 1);
                    Sort.Order corregido = new Sort.Order(dir, ordenAnterior.getProperty());
                    resultado.add(corregido);
                    ordenAnterior = corregido;
                }
            } else {
                resultado.add(new Sort.Order(order.getDirection(), prop));
                ordenAnterior = order;
            }
        }

        if (resultado.isEmpty()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(resultado));
    }
}
