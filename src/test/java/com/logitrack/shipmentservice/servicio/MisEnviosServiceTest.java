
package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.EnvioResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Cliente;
import com.logitrack.shipmentservice.dominio.entidad.Envio;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoEnvio;
import com.logitrack.shipmentservice.repositorio.EnvioRepository;
import com.logitrack.shipmentservice.servicio.impl.EnvioServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MisEnviosServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @InjectMocks
    private EnvioServiceImpl envioServicio;

    @Test
    void clienteConsultaSusPropiosEnvios() {
        String email = "cliente1@test.com";
        PageRequest pageable = PageRequest.of(0, 8);

        Cliente cliente = Cliente.builder()
                .email(email)
                .build();

        Envio envio = Envio.builder()
                .cliente(cliente)
                .codigoSeguimiento("LT-100001-TEST1")
                .estado(EstadoEnvio.PENDIENTE)
                .build();

        when(envioRepository.findByCliente_EmailIgnoreCase(email, pageable))
                .thenReturn(new PageImpl<>(List.of(envio), pageable, 1));


        Page<EnvioResponseDto> resultado =
                envioServicio.listarMisEnvios(email, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(
                "LT-100001-TEST1",
                resultado.getContent()
                        .get(0)
                        .codigoSeguimiento()
        );

        verify(envioRepository)
                .findByCliente_EmailIgnoreCase(email, pageable);
    }

    @Test
    void clienteNoPuedeConsultarTrackingAjeno() {
        String email = "cliente1@test.com";
        String codigoAjeno = "LT-100002-TEST2";


        when(envioRepository.findByCodigoSeguimientoAndCliente_EmailIgnoreCase(
                codigoAjeno,
                email
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> envioServicio.obtenerMiEnvioPorTracking(
                        email,
                        codigoAjeno
                )
        );
    }
}
