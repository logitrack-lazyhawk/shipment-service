package com.logitrack.shipmentservice.servicio.impl;

import com.logitrack.shipmentservice.dominio.dto.DireccionDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioRequestDto;
import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.dominio.valueObject.Direccion;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.servicio.EnvioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementación de la lógica de negocio para los envíos.
 * Coordina la generación de códigos hash únicos, cálculos de tarifa y persistencia.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class EnvioServiceImpl implements EnvioServicio {

    private final EnvioRepository envioRepository;

    @Override
    @Transactional
    public EnvioResponseDto registrarEnvio(EnvioRequestDto peticion) {
        Envio nuevoEnvio = mapearDesdeRequest(peticion);

        nuevoEnvio.setEstado(EstadoEnvio.PENDIENTE);
        nuevoEnvio.setTarifa(nuevoEnvio.calcularTarifa());
        nuevoEnvio.setCodigoSeguimiento(generarHashUnico());

        Envio envioGuardado = envioRepository.save(nuevoEnvio);

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
                mapearDireccionDto(envio.getDestino())
        );
    }

    private DireccionDto mapearDireccionDto(Direccion vo) {
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