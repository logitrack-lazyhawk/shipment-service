package com.logitrack.shipmentservice.servicio.impl;

import com.logitrack.shipmentservice.dominio.dto.DireccionDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Cliente;
import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.evento.EnvioEstadoCambiadoEvent;
import com.logitrack.shipmentservice.dominio.valueObject.Direccion;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.repositorio.HistorialEnvioRepository;
import com.logitrack.shipmentservice.servicio.EnvioServicio;
import com.logitrack.shipmentservice.servicio.util.PageableSanitizer;
import com.logitrack.shipmentservice.servicio.validador.ClienteValidator;
import com.logitrack.shipmentservice.servicio.validador.RepartidorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de la lógica de negocio para los envíos.
 * Coordina la generación de códigos hash únicos, cálculos de tarifa, persistencia y asignación de repartidores.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class EnvioServiceImpl implements EnvioServicio {

    private final EnvioRepository envioRepository;
    private final RepartidorValidator repartidorValidator;
    private final ClienteValidator clienteValidator;
    private final HistorialEnvioRepository historialRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PageableSanitizer pageableSanitizer;

    @Override
    @Transactional
    public EnvioResponseDto registrarEnvio(EnvioRequestDto peticion) {
        Cliente cliente = clienteValidator.validarYObtenerPorId(peticion.clienteId());

        Envio nuevoEnvio = mapearDesdeRequest(peticion);
        nuevoEnvio.setCliente(cliente);
        nuevoEnvio.setEstado(EstadoEnvio.PENDIENTE);
        nuevoEnvio.setTarifa(nuevoEnvio.calcularTarifa());
        nuevoEnvio.setCodigoSeguimiento(generarHashUnico());

        Envio envioGuardado = envioRepository.save(nuevoEnvio);

        return mapearHaciaResponse(envioGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioResponseDto obtenerPorId(UUID id) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: No se encontró un envío con el ID especificado."));
        return mapearHaciaResponse(envio);
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioResponseDto obtenerPorTracking(String codigo) {
        Envio envio = envioRepository.findByCodigoSeguimiento(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Error: No se encontró un envío con el código de seguimiento especificado."));
        return mapearHaciaResponse(envio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvioResponseDto> listarEnvios(EstadoEnvio estado) {
        List<Envio> listaEnvios;
        if (estado != null) {
            listaEnvios = envioRepository.findByEstado(estado);
        } else {
            listaEnvios = envioRepository.findAll();
        }

        return listaEnvios.stream()
                .map(this::mapearHaciaResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnvioResponseDto> listarEnviosPaginados(EstadoEnvio estado, Pageable pageable) {
        Pageable pageableSanitizado = (pageableSanitizer != null) ? pageableSanitizer.sanitizar(pageable) : pageable;

        Page<Envio> paginaEnvios = (estado != null)
                ? envioRepository.findByEstado(estado, pageableSanitizado)
                : envioRepository.findAll(pageableSanitizado);

        return paginaEnvios.map(this::mapearHaciaResponse);
    }

    @Override
    @Transactional
    public EnvioResponseDto cancelarEnvio(UUID id) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: No se encontró un envío con el ID especificado."));

        envio.cancelar();
        Envio envioGuardado = envioRepository.save(envio);

        return mapearHaciaResponse(envioGuardado);
    }

    @Override
    @Transactional
    public EnvioResponseDto cambiarEstado(UUID id, EstadoEnvio nuevoEstado) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: No se encontró un envío con el ID especificado."));

        EstadoEnvio estadoAnterior = envio.getEstado();

        // 1. La entidad valida y cambia el estado
        envio.cambiarEstado(nuevoEstado);
        Envio envioActualizado = envioRepository.save(envio);

        // 2. Publicamos el evento
        eventPublisher.publishEvent(new EnvioEstadoCambiadoEvent(
                envioActualizado.getId(),
                estadoAnterior,
                envioActualizado.getEstado(),
                LocalDateTime.now()
        ));

        return mapearHaciaResponse(envioActualizado);
    }

    @Override
    @Transactional
    public EnvioResponseDto asignarRepartidor(UUID envioId, UUID repartidorId) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new IllegalArgumentException("Error: No se encontró un envío con el ID especificado."));

        // Validar disponibilidad remota en courier-service
        repartidorValidator.validarDisponibilidadRemota(repartidorId);

        // Validaciones de negocio y asignación en la entidad
        envio.asignarRepartidor(repartidorId);
        Envio envioGuardado = envioRepository.save(envio);

        return mapearHaciaResponse(envioGuardado);
    }

    private String generarHashUnico() {
        String hashGenerado;
        do {
            String marcaTiempo = String.valueOf(Instant.now().toEpochMilli());
            String sufijoAleatorio = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            hashGenerado = "LT-" + marcaTiempo.substring(marcaTiempo.length() - 6) + "-" + sufijoAleatorio;
        } while (envioRepository.existsByCodigoSeguimiento(hashGenerado));

        return hashGenerado;
    }

    private Envio mapearDesdeRequest(EnvioRequestDto dto) {
        return Envio.builder()
                .peso(dto.peso())
                .ancho(dto.ancho())
                .alto(dto.alto())
                .largo(dto.largo())
                .origen(mapearDireccionVO(dto.origen()))
                .destino(mapearDireccionVO(dto.destino()))
                .build();
    }

    private Direccion mapearDireccionVO(DireccionDto dto) {
        if (dto == null) {
            return null;
        }
        return Direccion.builder()
                .calle(dto.calle())
                .numero(dto.numero())
                .ciudad(dto.ciudad())
                .codigoPostal(dto.codigoPostal())
                .provincia(dto.provincia())
                .localidad(dto.localidad())
                .descripcion(dto.descripcion())
                .build();
    }

    private EnvioResponseDto mapearHaciaResponse(Envio envio) {
        return new EnvioResponseDto(
                envio.getId(),
                envio.getCodigoSeguimiento(),
                envio.getPeso(),
                envio.getAncho(),
                envio.getAlto(),
                envio.getLargo(),
                envio.getTarifa(),
                envio.getEstado(),
                mapearDireccionDto(envio.getOrigen()),
                mapearDireccionDto(envio.getDestino()),
                envio.getRepartidorId(),
                envio.getCliente() != null ? envio.getCliente().getId() : null
        );
    }

    private DireccionDto mapearDireccionDto(Direccion vo) {
        if (vo == null) {
            return null;
        }
        return new DireccionDto(
                vo.getCalle(),
                vo.getNumero(),
                vo.getCiudad(),
                vo.getCodigoPostal(),
                vo.getProvincia(),
                vo.getLocalidad(),
                vo.getDescripcion()
        );
    }
}
