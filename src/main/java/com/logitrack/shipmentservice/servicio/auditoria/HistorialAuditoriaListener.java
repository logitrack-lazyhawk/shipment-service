package com.logitrack.shipmentservice.servicio.auditoria;

import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.entidad.HistorialEnvio;
import com.logitrack.shipmentservice.dominio.evento.EnvioEstadoCambiadoEvent;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.repositorio.HistorialEnvioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistorialAuditoriaListener {

    private final HistorialEnvioRepository historialRepository;
    private final EnvioRepository envioRepository;

    @EventListener
    public void handleEnvioEstadoCambiado(EnvioEstadoCambiadoEvent event) {
        Envio envio = envioRepository.getReferenceById(event.envioId());

        // Creamos el registro inmutable
        HistorialEnvio historial = HistorialEnvio.builder()
                .envio(envio)
                .estadoAnterior(event.estadoAnterior() != null ? event.estadoAnterior().name() : "NUEVO")
                .estadoNuevo(event.estadoNuevo().name())
                .fechaHora(event.fechaHora())
                .usuarioActor("SISTEMA")
                .build();

        historialRepository.save(historial);

        System.out.println("✅ Auditoría guardada para el envío: " + event.envioId());
    }
}
