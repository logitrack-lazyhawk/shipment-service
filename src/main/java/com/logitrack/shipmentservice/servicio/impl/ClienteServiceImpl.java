package com.logitrack.shipmentservice.servicio.impl;

import com.logitrack.shipmentservice.dominio.dto.ClienteRequestDto;
import com.logitrack.shipmentservice.dominio.dto.ClienteResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Cliente;
import com.logitrack.shipmentservice.repositorio.ClienteRepository;
import com.logitrack.shipmentservice.servicio.ClienteServicio;
import com.logitrack.shipmentservice.servicio.mapper.ClienteMapper;
import com.logitrack.shipmentservice.servicio.util.PageableSanitizer;
import com.logitrack.shipmentservice.servicio.validador.ClienteValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de aplicación que orquesta los casos de uso para clientes.
 * Aplica el principio de responsabilidad única delegando en validadores, mappers y utilitarios.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteServicio {

    private final ClienteRepository clienteRepository;
    private final ClienteValidator clienteValidator;
    private final ClienteMapper clienteMapper;
    private final PageableSanitizer pageableSanitizer;

    @Override
    @Transactional
    public ClienteResponseDto registrarCliente(ClienteRequestDto peticion) {
        log.debug("Iniciando registro de cliente: '{}' con DNI: '{}'", peticion.nombre(), peticion.dni());

        clienteValidator.validarParaCreacion(peticion);

        Cliente nuevoCliente = clienteMapper.toEntity(peticion);
        Cliente clienteGuardado = clienteRepository.save(nuevoCliente);

        log.info("Cliente registrado exitosamente con ID: '{}', email: '{}'",
                clienteGuardado.getId(), clienteGuardado.getEmail());

        return clienteMapper.toDto(clienteGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerPorId(UUID id) {
        log.debug("Buscando cliente por ID: '{}'", id);
        Cliente cliente = clienteValidator.validarYObtenerPorId(id);
        log.info("Cliente recuperado exitosamente con ID: '{}', nombre: '{}'", cliente.getId(), cliente.getNombre());
        return clienteMapper.toDto(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDto> listarClientes(Pageable pageable) {
        Pageable pageableSanitizado = pageableSanitizer.sanitizar(pageable);

        log.debug("Consultando lista paginada de clientes. Página: {}, tamaño: {}",
                pageableSanitizado.getPageNumber(), pageableSanitizado.getPageSize());

        Page<Cliente> pagina = clienteRepository.findAll(pageableSanitizado);

        log.info("Consulta paginada de clientes completada. Elementos: {}, Total páginas: {}, Total general: {}",
                pagina.getNumberOfElements(), pagina.getTotalPages(), pagina.getTotalElements());

        if (pagina.isEmpty()) {
            log.warn("La página consultada no arrojó resultados.");
        }

        return pagina.map(clienteMapper::toDto);
    }

    @Override
    @Transactional
    public ClienteResponseDto actualizarCliente(UUID id, ClienteRequestDto peticion) {
        log.debug("Iniciando actualización de cliente con ID: '{}'", id);

        Cliente cliente = clienteValidator.validarYObtenerPorId(id);
        clienteValidator.validarParaActualizacion(id, peticion);

        clienteMapper.actualizarEntidad(cliente, peticion);
        Cliente clienteActualizado = clienteRepository.save(cliente);

        log.info("Cliente con ID: '{}' actualizado exitosamente.", id);
        return clienteMapper.toDto(clienteActualizado);
    }

    @Override
    @Transactional
    public void eliminarCliente(UUID id) {
        log.debug("Iniciando baja lógica de cliente con ID: '{}'", id);
        Cliente cliente = clienteValidator.validarYObtenerPorId(id);

        clienteRepository.delete(cliente);
        log.info("Cliente con ID: '{}' dado de baja exitosamente (soft delete aplicado)", id);
    }
}
