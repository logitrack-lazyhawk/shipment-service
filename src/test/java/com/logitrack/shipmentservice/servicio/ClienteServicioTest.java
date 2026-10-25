package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.ClienteRequestDto;
import com.logitrack.shipmentservice.dominio.dto.ClienteResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Cliente;
import com.logitrack.shipmentservice.repositorio.ClienteRepository;
import com.logitrack.shipmentservice.servicio.impl.ClienteServiceImpl;
import com.logitrack.shipmentservice.servicio.mapper.ClienteMapper;
import com.logitrack.shipmentservice.servicio.util.PageableSanitizer;
import com.logitrack.shipmentservice.servicio.validador.ClienteValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServicioTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteValidator clienteValidator;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private PageableSanitizer pageableSanitizer;

    @InjectMocks
    private ClienteServiceImpl clienteServicio;

    private UUID clienteId;
    private Cliente cliente;
    private ClienteRequestDto requestDto;
    private ClienteResponseDto responseDto;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();

        cliente = Cliente.builder()
                .nombre("Juan Pérez")
                .dni("30111222")
                .telefono("+541144332211")
                .email("juan.perez@example.com")
                .build();
        cliente.setId(clienteId);

        requestDto = new ClienteRequestDto("Juan Pérez", "30.111.222", "11-4433-2211", "juan.perez@example.com");
        responseDto = new ClienteResponseDto(clienteId, "Juan Pérez", "30111222", "+541144332211", "juan.perez@example.com");
    }

    @Test
    @DisplayName("registrarCliente: Valida, persiste y retorna el DTO del cliente")
    void registrarCliente_Exitoso() {
        doNothing().when(clienteValidator).validarParaCreacion(requestDto);
        when(clienteMapper.toEntity(requestDto)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toDto(cliente)).thenReturn(responseDto);

        ClienteResponseDto resultado = clienteServicio.registrarCliente(requestDto);

        assertNotNull(resultado);
        assertEquals(clienteId, resultado.id());
        assertEquals("Juan Pérez", resultado.nombre());
        assertEquals("30111222", resultado.dni());
        assertEquals("+541144332211", resultado.telefono());
        assertEquals("juan.perez@example.com", resultado.email());

        verify(clienteValidator).validarParaCreacion(requestDto);
        verify(clienteMapper).toEntity(requestDto);
        verify(clienteRepository).save(cliente);
        verify(clienteMapper).toDto(cliente);
    }

    @Test
    @DisplayName("obtenerPorId: Retorna DTO cuando el cliente existe")
    void obtenerPorId_Existe_RetornaDto() {
        when(clienteValidator.validarYObtenerPorId(clienteId)).thenReturn(cliente);
        when(clienteMapper.toDto(cliente)).thenReturn(responseDto);

        ClienteResponseDto resultado = clienteServicio.obtenerPorId(clienteId);

        assertNotNull(resultado);
        assertEquals(clienteId, resultado.id());
        verify(clienteValidator).validarYObtenerPorId(clienteId);
        verify(clienteMapper).toDto(cliente);
    }

    @Test
    @DisplayName("obtenerPorId: Propaga excepción cuando el cliente no existe")
    void obtenerPorId_NoExiste_PropagaExcepcion() {
        when(clienteValidator.validarYObtenerPorId(clienteId))
                .thenThrow(new IllegalArgumentException("Error: No se encontró un cliente con el ID especificado."));

        assertThrows(IllegalArgumentException.class, () ->
                clienteServicio.obtenerPorId(clienteId)
        );

        verify(clienteValidator).validarYObtenerPorId(clienteId);
        verify(clienteMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("listarClientes: Retorna página de clientes sanitizada")
    void listarClientes_RetornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        when(pageableSanitizer.sanitizar(pageable)).thenReturn(pageable);

        Page<Cliente> pagina = new PageImpl<>(List.of(cliente), pageable, 1);
        when(clienteRepository.findAll(pageable)).thenReturn(pagina);
        when(clienteMapper.toDto(cliente)).thenReturn(responseDto);

        Page<ClienteResponseDto> resultado = clienteServicio.listarClientes(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(clienteId, resultado.getContent().get(0).id());

        verify(clienteRepository).findAll(pageable);
    }

    @Test
    @DisplayName("actualizarCliente: Valida, actualiza entidad, persiste y retorna DTO")
    void actualizarCliente_Exitoso() {
        ClienteRequestDto updateDto = new ClienteRequestDto("Juan Pérez Actualizado", "30111222", "+541144332211", "juan.nuevo@example.com");
        ClienteResponseDto updatedResponse = new ClienteResponseDto(clienteId, "Juan Pérez Actualizado", "30111222", "+541144332211", "juan.nuevo@example.com");

        when(clienteValidator.validarYObtenerPorId(clienteId)).thenReturn(cliente);
        doNothing().when(clienteValidator).validarParaActualizacion(clienteId, updateDto);
        doNothing().when(clienteMapper).actualizarEntidad(cliente, updateDto);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toDto(cliente)).thenReturn(updatedResponse);

        ClienteResponseDto resultado = clienteServicio.actualizarCliente(clienteId, updateDto);

        assertNotNull(resultado);
        assertEquals("Juan Pérez Actualizado", resultado.nombre());
        assertEquals("juan.nuevo@example.com", resultado.email());

        verify(clienteValidator).validarYObtenerPorId(clienteId);
        verify(clienteValidator).validarParaActualizacion(clienteId, updateDto);
        verify(clienteMapper).actualizarEntidad(cliente, updateDto);
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("eliminarCliente: Valida existencia y ejecuta baja lógica en repositorio")
    void eliminarCliente_Exitoso() {
        when(clienteValidator.validarYObtenerPorId(clienteId)).thenReturn(cliente);

        clienteServicio.eliminarCliente(clienteId);

        verify(clienteValidator).validarYObtenerPorId(clienteId);
        verify(clienteRepository).delete(cliente);
    }
}
