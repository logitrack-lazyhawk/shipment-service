package com.logitrack.shipmentservice.dominio.evento;

import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import java.time.LocalDateTime;
import java.util.UUID;

public record EnvioEstadoCambiadoEvent(
        UUID envioId,
        EstadoEnvio estadoAnterior,
        EstadoEnvio estadoNuevo,
        LocalDateTime fechaHora
) {}
