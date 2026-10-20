package com.logitrack.shipmentservice.servicio.validador;

import com.logitrack.shipmentservice.dominio.entidad.Repartidor;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import com.logitrack.shipmentservice.repositorio.RepartidorRepository;
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
class RepartidorValidatorTest {

    @Mock
    private RepartidorRepository repartidorRepository;

    @InjectMocks
    private RepartidorValidator repartidorValidator;

    private UUID repartidorId;
    private Repartidor repartidorActivo;
    private Repartidor repartidorInactivo;

    @BeforeEach
    void setUp() {
        repartidorId = UUID.randomUUID();

        repartidorActivo = Repartidor.builder()
                .nombre("Carlos Gomez")
                .tipoVehiculo("Moto")
                .estado(EstadoRepartidor.ACTIVO)
                .build();
        repartidorActivo.setId(repartidorId);

        repartidorInactivo = Repartidor.builder()
                .nombre("Mariana Lopez")
                .tipoVehiculo("Auto")
                .estado(EstadoRepartidor.INACTIVO)
                .build();
        repartidorInactivo.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("validarYObtenerPorId: Retorna el repartidor si existe en la base de datos")
    void validarYObtenerPorId_Existe_RetornaRepartidor() {
        when(repartidorRepository.findById(repartidorId)).thenReturn(Optional.of(repartidorActivo));

        Repartidor resultado = repartidorValidator.validarYObtenerPorId(repartidorId);

        assertNotNull(resultado);
        assertEquals(repartidorId, resultado.getId());
        assertEquals("Carlos Gomez", resultado.getNombre());
    }

    @Test
    @DisplayName("validarYObtenerPorId: Lanza IllegalArgumentException si el repartidor no existe")
    void validarYObtenerPorId_NoExiste_LanzaExcepcion() {
        UUID idInexistente = UUID.randomUUID();
        when(repartidorRepository.findById(idInexistente)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                repartidorValidator.validarYObtenerPorId(idInexistente)
        );

        assertTrue(ex.getMessage().contains("No se encontró un repartidor con el ID"));
    }

    @Test
    @DisplayName("validarActivoParaOperacion: Pasa exitosamente cuando el repartidor está ACTIVO")
    void validarActivoParaOperacion_Activo_PasaExitosamente() {
        assertDoesNotThrow(() -> repartidorValidator.validarActivoParaOperacion(repartidorActivo));
    }

    @Test
    @DisplayName("validarActivoParaOperacion: Lanza ReglaNegocioException cuando el repartidor está INACTIVO")
    void validarActivoParaOperacion_Inactivo_LanzaReglaNegocioException() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                repartidorValidator.validarActivoParaOperacion(repartidorInactivo)
        );

        assertTrue(ex.getMessage().contains("debe estar estrictamente en estado ACTIVO"));
    }

    @Test
    @DisplayName("validarActivoParaOperacion: Lanza IllegalArgumentException cuando el repartidor es null")
    void validarActivoParaOperacion_Null_LanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                repartidorValidator.validarActivoParaOperacion(null)
        );
    }
}
