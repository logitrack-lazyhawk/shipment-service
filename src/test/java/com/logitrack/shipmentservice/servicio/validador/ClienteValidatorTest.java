package com.logitrack.shipmentservice.servicio.validador;

import com.logitrack.shipmentservice.dominio.dto.ClienteRequestDto;
import com.logitrack.shipmentservice.dominio.entidad.Cliente;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.repositorio.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteValidatorTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteValidator clienteValidator;

    private UUID clienteId;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        cliente = Cliente.builder()
                .nombre("Juan Pérez")
                .dni("30111222")
                .telefono("+541144332211")
                .email("juan@example.com")
                .build();
        cliente.setId(clienteId);
    }

    @Test
    @DisplayName("validarYObtenerPorId: Retorna el cliente si existe")
    void validarYObtenerPorId_Existe_RetornaCliente() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteValidator.validarYObtenerPorId(clienteId);

        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getId());
        assertEquals("Juan Pérez", resultado.getNombre());
    }

    @Test
    @DisplayName("validarYObtenerPorId: Lanza IllegalArgumentException si el cliente no existe")
    void validarYObtenerPorId_NoExiste_LanzaExcepcion() {
        UUID idInexistente = UUID.randomUUID();
        when(clienteRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                clienteValidator.validarYObtenerPorId(idInexistente)
        );
    }

    @Test
    @DisplayName("validarParaCreacion: Lanza ReglaNegocioException si el DNI ya existe")
    void validarParaCreacion_DniDuplicado_LanzaExcepcion() {
        ClienteRequestDto dto = new ClienteRequestDto("Pedro", "30111222", "+541144332211", "pedro@example.com");
        when(clienteRepository.existsByDni("30111222")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () ->
                clienteValidator.validarParaCreacion(dto)
        );
    }

    @Test
    @DisplayName("validarParaCreacion: Lanza ReglaNegocioException si el email ya existe")
    void validarParaCreacion_EmailDuplicado_LanzaExcepcion() {
        ClienteRequestDto dto = new ClienteRequestDto("Pedro", "32999888", "+541144332211", "juan@example.com");
        when(clienteRepository.existsByDni("32999888")).thenReturn(false);
        when(clienteRepository.existsByEmail("juan@example.com")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () ->
                clienteValidator.validarParaCreacion(dto)
        );
    }

    @Test
    @DisplayName("validarParaActualizacion: Lanza ReglaNegocioException si el DNI pertenece a otro cliente")
    void validarParaActualizacion_DniPerteneceAOtro_LanzaExcepcion() {
        ClienteRequestDto dto = new ClienteRequestDto("Pedro", "30111222", "+541144332211", "pedro@example.com");
        when(clienteRepository.existsByDniAndIdNot("30111222", clienteId)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () ->
                clienteValidator.validarParaActualizacion(clienteId, dto)
        );
    }

    @Test
    @DisplayName("validarParaActualizacion: Lanza ReglaNegocioException si el email pertenece a otro cliente")
    void validarParaActualizacion_EmailPerteneceAOtro_LanzaExcepcion() {
        ClienteRequestDto dto = new ClienteRequestDto("Pedro", "30111222", "+541144332211", "otro@example.com");
        when(clienteRepository.existsByDniAndIdNot("30111222", clienteId)).thenReturn(false);
        when(clienteRepository.existsByEmailAndIdNot("otro@example.com", clienteId)).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () ->
                clienteValidator.validarParaActualizacion(clienteId, dto)
        );
    }
}
