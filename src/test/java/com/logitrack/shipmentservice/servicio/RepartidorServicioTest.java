package com.logitrack.shipmentservice.servicio;

import com.logitrack.shipmentservice.dominio.dto.RepartidorRequestDto;
import com.logitrack.shipmentservice.dominio.dto.RepartidorResponseDto;
import com.logitrack.shipmentservice.dominio.entidad.Repartidor;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import com.logitrack.shipmentservice.repositorio.RepartidorRepository;
import com.logitrack.shipmentservice.servicio.impl.RepartidorServiceImpl;
import com.logitrack.shipmentservice.servicio.mapper.RepartidorMapper;
import com.logitrack.shipmentservice.servicio.util.PageableSanitizer;
import com.logitrack.shipmentservice.servicio.validador.RepartidorValidator;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepartidorServicioTest {

    @Mock
    private RepartidorRepository repartidorRepository;

    @Mock
    private RepartidorValidator repartidorValidator;

    @Mock
    private RepartidorMapper repartidorMapper;

    @Mock
    private PageableSanitizer pageableSanitizer;

    @InjectMocks
    private RepartidorServiceImpl repartidorServicio;

    private UUID repartidorId;
    private Repartidor repartidor;
    private RepartidorRequestDto requestDto;
    private RepartidorResponseDto responseDto;

    @BeforeEach
    void setUp() {
        repartidorId = UUID.randomUUID();

        repartidor = Repartidor.builder()
                .nombre("Carlos Gomez")
                .dni("30111222")
                .telefono("+541144332211")
                .tipoVehiculo("Moto")
                .estado(EstadoRepartidor.ACTIVO)
                .build();
        repartidor.setId(repartidorId);

        requestDto = new RepartidorRequestDto("Carlos Gomez", "30.111.222", "11-4433-2211", "Moto", EstadoRepartidor.ACTIVO);
        responseDto = new RepartidorResponseDto(repartidorId, "Carlos Gomez", "30111222", "+541144332211", "Moto", EstadoRepartidor.ACTIVO);
    }

    @Test
    @DisplayName("registrarRepartidor: Registra y retorna el DTO del repartidor persistido con datos limpios y normalizados")
    void registrarRepartidor_Exitoso() {
        when(repartidorMapper.toEntity(requestDto)).thenReturn(repartidor);
        when(repartidorRepository.save(repartidor)).thenReturn(repartidor);
        when(repartidorMapper.toDto(repartidor)).thenReturn(responseDto);

        RepartidorResponseDto resultado = repartidorServicio.registrarRepartidor(requestDto);

        assertNotNull(resultado);
        assertEquals(repartidorId, resultado.id());
        assertEquals("Carlos Gomez", resultado.nombre());
        assertEquals("30111222", resultado.dni());
        assertEquals("+541144332211", resultado.telefono());
        assertEquals(EstadoRepartidor.ACTIVO, resultado.estado());

        verify(repartidorMapper).toEntity(requestDto);
        verify(repartidorRepository).save(repartidor);
        verify(repartidorMapper).toDto(repartidor);
    }

    @Test
    @DisplayName("obtenerPorId: Retorna DTO cuando el repartidor existe")
    void obtenerPorId_Existe_RetornaDto() {
        when(repartidorValidator.validarYObtenerPorId(repartidorId)).thenReturn(repartidor);
        when(repartidorMapper.toDto(repartidor)).thenReturn(responseDto);

        RepartidorResponseDto resultado = repartidorServicio.obtenerPorId(repartidorId);

        assertNotNull(resultado);
        assertEquals(repartidorId, resultado.id());
        verify(repartidorValidator).validarYObtenerPorId(repartidorId);
        verify(repartidorMapper).toDto(repartidor);
    }

    @Test
    @DisplayName("obtenerPorId: Lanza excepción cuando el validador no encuentra el ID")
    void obtenerPorId_NoExiste_PropagaExcepcion() {
        when(repartidorValidator.validarYObtenerPorId(repartidorId))
                .thenThrow(new IllegalArgumentException("Error: No se encontró un repartidor con el ID especificado."));

        assertThrows(IllegalArgumentException.class, () ->
                repartidorServicio.obtenerPorId(repartidorId)
        );

        verify(repartidorValidator).validarYObtenerPorId(repartidorId);
        verify(repartidorMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("listarRepartidores: Retorna página filtrada cuando se pasa estado")
    void listarRepartidores_ConEstado_RetornaPaginaFiltrada() {
        Pageable pageable = PageRequest.of(0, 10);
        when(pageableSanitizer.sanitizar(pageable)).thenReturn(pageable);

        Page<Repartidor> paginaEntidades = new PageImpl<>(List.of(repartidor), pageable, 1);
        when(repartidorRepository.findByEstado(EstadoRepartidor.ACTIVO, pageable)).thenReturn(paginaEntidades);
        when(repartidorMapper.toDto(repartidor)).thenReturn(responseDto);

        Page<RepartidorResponseDto> resultado = repartidorServicio.listarRepartidores(EstadoRepartidor.ACTIVO, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(repartidorId, resultado.getContent().get(0).id());

        verify(repartidorRepository).findByEstado(EstadoRepartidor.ACTIVO, pageable);
        verify(repartidorRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("listarRepartidores: Retorna todos cuando el estado es null")
    void listarRepartidores_SinEstado_RetornaTodos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(pageableSanitizer.sanitizar(pageable)).thenReturn(pageable);

        Page<Repartidor> paginaEntidades = new PageImpl<>(List.of(repartidor), pageable, 1);
        when(repartidorRepository.findAll(pageable)).thenReturn(paginaEntidades);
        when(repartidorMapper.toDto(repartidor)).thenReturn(responseDto);

        Page<RepartidorResponseDto> resultado = repartidorServicio.listarRepartidores(null, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());

        verify(repartidorRepository).findAll(pageable);
        verify(repartidorRepository, never()).findByEstado(any(), any());
    }

    @Test
    @DisplayName("eliminarRepartidor: Valida existencia y ejecuta borrado lógico por repositorio")
    void eliminarRepartidor_Exitoso() {
        when(repartidorValidator.validarYObtenerPorId(repartidorId)).thenReturn(repartidor);

        repartidorServicio.eliminarRepartidor(repartidorId);

        verify(repartidorValidator).validarYObtenerPorId(repartidorId);
        verify(repartidorRepository).delete(repartidor);
    }
}
