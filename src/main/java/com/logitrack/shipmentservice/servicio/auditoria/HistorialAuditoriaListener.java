package com.logitrack.shipmentservice.servicio.auditoria;

import com.logitrack.shipmentservice.dominio.entidad.HistorialEnvio;
import com.logitrack.shipmentservice.dominio.evento.EnvioEstadoCambiadoEvent;
import com.logitrack.shipmentservice.repositorio.HistorialEnvioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistorialAuditoriaListener {

    private final HistorialEnvioRepository historialRepository;

    @EventListener
    public void handleEnvioEstadoCambiado(EnvioEstadoCambiadoEvent event) {
        // Creamos el registro inmutable
        HistorialEnvio historial = HistorialEnvio.builder()
                .envioId(event.envioId())
                .estadoAnterior(event.estadoAnterior() != null ? event.estadoAnterior().name() : "NUEVO")
                .estadoNuevo(event.estadoNuevo().name())
                .fechaHora(event.fechaHora())
                .usuarioActor("SISTEMA")
                .build();

        historialRepository.save(historial);

        System.out.println("✅ Auditoría guardada para el envío: " + event.envioId());
    }
}
