package com.logitrack.shipmentservice.controlador.exception;

import com.logitrack.shipmentservice.dominio.exception.TransicionEstadoInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Capturamos nuestra excepción de dominio
    @ExceptionHandler(TransicionEstadoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleTransicionInvalida(TransicionEstadoInvalidaException ex) {

        // Creamos una respuesta JSON clara para el cliente
        Map<String, Object> respuesta = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(), // 400
                "error", "Transición de Estado Inválida",
                "message", ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // Aquí podrías agregar más @ExceptionHandler para otras excepciones (404, 422, etc.)
}
