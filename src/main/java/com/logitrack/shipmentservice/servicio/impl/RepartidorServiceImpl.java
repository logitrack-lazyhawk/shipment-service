package com.logitrack.shipmentservice.servicio.impl;

import com.logitrack.shipmentservice.dominio.dto.RepartidorRequestDto;
import com.logitrack.shipmentservice.dominio.dto.RepartidorResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Repartidor;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import com.logitrack.shipmentservice.repositorio.RepartidorRepository;
import com.logitrack.shipmentservice.servicio.RepartidorServicio;
import com.logitrack.shipmentservice.servicio.mapper.RepartidorMapper;
import com.logitrack.shipmentservice.servicio.util.PageableSanitizer;
import com.logitrack.shipmentservice.servicio.validador.RepartidorValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de aplicación que orquesta los casos de uso de repartidores.
 * Aplica el principio de responsabilidad única delegando en validadores, mappers y utilitarios.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepartidorServiceImpl implements RepartidorServicio {

    private final RepartidorRepository repartidorRepository;
    private final RepartidorValidator repartidorValidator;
    private final RepartidorMapper repartidorMapper;
    private final PageableSanitizer pageableSanitizer;

    @Override
    @Transactional
    public RepartidorResponseDto registrarRepartidor(RepartidorRequestDto peticion) {
        log.debug("Iniciando registro de repartidor: '{}'", peticion.nombre());

        try {
            Repartidor nuevoRepartidor = repartidorMapper.toEntity(peticion);
            Repartidor repartidorGuardado = repartidorRepository.save(nuevoRepartidor);

            log.info("Repartidor registrado exitosamente con ID: '{}', estado inicial: '{}'",
                    repartidorGuardado.getId(), repartidorGuardado.getEstado());

            return repartidorMapper.toDto(repartidorGuardado);
        } catch (Exception e) {
            log.error("Error al persistir el repartidor '{}': {}", peticion.nombre(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RepartidorResponseDto obtenerPorId(UUID id) {
        Repartidor repartidor = repartidorValidator.validarYObtenerPorId(id);
        log.info("Repartidor recuperado exitosamente con ID: '{}', estado: '{}'", repartidor.getId(), repartidor.getEstado());
        return repartidorMapper.toDto(repartidor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RepartidorResponseDto> listarRepartidores(EstadoRepartidor estado, Pageable pageable) {
        Pageable pageableSanitizado = pageableSanitizer.sanitizar(pageable);

        log.debug("Consultando lista paginada de repartidores con filtro: '{}', página: {}, tamaño: {}",
                estado, pageableSanitizado.getPageNumber(), pageableSanitizado.getPageSize());

        Page<Repartidor> pagina = (estado != null)
                ? repartidorRepository.findByEstado(estado, pageableSanitizado)
                : repartidorRepository.findAll(pageableSanitizado);

        log.info("Consulta paginada completada. Elementos: {}, Total páginas: {}, Total general: {}",
                pagina.getNumberOfElements(), pagina.getTotalPages(), pagina.getTotalElements());

        if (pagina.isEmpty()) {
            log.warn("La página consultada no arrojó resultados para el filtro: '{}'", estado);
        }

        return pagina.map(repartidorMapper::toDto);
    }

    @Override
    @Transactional
    public void eliminarRepartidor(UUID id) {
        log.debug("Iniciando baja lógica de repartidor con ID: '{}'", id);
        Repartidor repartidor = repartidorValidator.validarYObtenerPorId(id);

        repartidorRepository.delete(repartidor);
        log.info("Repartidor con ID: '{}' dado de baja exitosamente (soft delete aplicado)", id);
    }
}
