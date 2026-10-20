package com.logitrack.shipmentservice.dominio.dto;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Objeto de transferencia utilizado para recibir la petición de creación de un repartidor.
 * Incluye atributos comunes de identificación y contacto heredados conceptualmente de Persona.
 * Aplica limpieza automática previa (remueve '.', '-', espacios y paréntesis) y normaliza
 * el teléfono al formato internacional E.164 para Argentina (+54).
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
public record RepartidorRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El DNI es obligatorio")
        @Size(max = 8, message = "El DNI no puede superar los 8 dígitos")
        @Pattern(regexp = "^\\d{7,8}$", message = "El DNI debe contener 7 u 8 dígitos numéricos (sin puntos ni espacios)")
        String dni,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(
                regexp = "^\\+54\\d{8,11}$",
                message = "El teléfono debe tener formato internacional E.164 (+54 seguido del código de área sin 0 y número sin 15, con longitud total entre 10 y 13 dígitos)"
        )
        String telefono,

        @NotBlank(message = "El tipo de vehículo es obligatorio")
        @Size(max = 50, message = "El tipo de vehículo no puede superar los 50 caracteres")
        String tipoVehiculo,

        EstadoRepartidor estado
) {
    public RepartidorRequestDto {
        if (dni != null) {
            dni = limpiarDni(dni);
        }
        if (telefono != null) {
            telefono = normalizarTelefono(telefono);
        }
    }

    public RepartidorRequestDto(String nombre, String dni, String telefono, String tipoVehiculo) {
        this(nombre, dni, telefono, tipoVehiculo, EstadoRepartidor.ACTIVO);
    }

    private static String limpiarDni(String raw) {
        return raw.replaceAll("[.\\-\\s]", "").trim();
    }

    private static String normalizarTelefono(String raw) {
        String limpio = raw.replaceAll("[.\\-\\s()]", "").trim();
        if (limpio.isBlank()) {
            return limpio;
        }

        boolean teniaSignoMas = limpio.startsWith("+");
        if (teniaSignoMas) {
            limpio = limpio.substring(1);
        }

        // Si comienza con código de país 54
        if (limpio.startsWith("54")) {
            String resto = limpio.substring(2);
            boolean tieneNueve = resto.startsWith("9");
            if (tieneNueve) {
                resto = resto.substring(1);
            }
            if (resto.startsWith("0")) {
                resto = resto.substring(1);
            }
            resto = removerPrefijoQuince(resto);
            return "+" + (tieneNueve ? "549" : "54") + resto;
        }

        // Formato nacional: si comienza con 0, remover código de acceso nacional
        if (limpio.startsWith("0")) {
            limpio = limpio.substring(1);
        }

        // Remover prefijo 15 de telefonía móvil local si está presente
        limpio = removerPrefijoQuince(limpio);

        // Si tiene entre 8 y 11 dígitos numéricos nacionales (ej: 10 dígitos área + local), anteponer +54
        if (limpio.matches("\\d{8,11}")) {
            return "+54" + limpio;
        }

        // Si ya venía con formato internacional o no clasificó, preservar para evaluación de @Pattern
        return teniaSignoMas ? "+" + limpio : limpio;
    }

    private static String removerPrefijoQuince(String numero) {
        if (numero == null) {
            return null;
        }
        // Si tiene 12 dígitos, verificar si contiene '15' según longitud de código de área (2, 3 o 4 dígitos)
        if (numero.length() == 12 && numero.matches("\\d+")) {
            // Área de 2 dígitos (ej: 11 15 xxxxxxxx)
            if (numero.startsWith("1115")) {
                return "11" + numero.substring(4);
            }
            // Área de 3 dígitos (ej: 351 15 xxxxxxx)
            if ("15".equals(numero.substring(3, 5))) {
                return numero.substring(0, 3) + numero.substring(5);
            }
            // Área de 4 dígitos (ej: 2966 15 xxxxxx)
            if ("15".equals(numero.substring(4, 6))) {
                return numero.substring(0, 4) + numero.substring(6);
            }
        }
        return numero;
    }
}
