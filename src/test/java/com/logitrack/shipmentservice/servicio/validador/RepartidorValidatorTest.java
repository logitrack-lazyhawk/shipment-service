package com.logitrack.shipmentservice.servicio.validador;

import com.logitrack.shipmentservice.cliente.courier.CourierClient;
import com.logitrack.shipmentservice.cliente.courier.dto.RepartidorClientResponseDto;
import com.logitrack.shipmentservice.dominio.exception.ReglaNegocioException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para RepartidorValidator en su integración con CourierClient (OpenFeign).
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class RepartidorValidatorTest {

    @Mock
    private CourierClient courierClient;

    @InjectMocks
    private RepartidorValidator repartidorValidator;

    private UUID repartidorId;
    private RepartidorClientResponseDto repartidorActivoDto;

    @BeforeEach
    void setUp() {
        repartidorId = UUID.randomUUID();
        repartidorActivoDto = new RepartidorClientResponseDto(
                repartidorId,
                "Carlos Gomez",
                "30111222",
                "+541144332211",
                "carlos@email.com",
                "Moto",
                "ACTIVO"
        );
    }

    @Test
    @DisplayName("validarDisponibilidadRemota: Retorna DTO cuando el repartidor existe y está ACTIVO")
    void validarDisponibilidadRemota_RepartidorActivo_RetornaDto() {
        when(courierClient.consultarDisponibilidad(repartidorId)).thenReturn(repartidorActivoDto);

        RepartidorClientResponseDto resultado = repartidorValidator.validarDisponibilidadRemota(repartidorId);

        assertNotNull(resultado);
        assertEquals(repartidorId, resultado.id());
        assertEquals("ACTIVO", resultado.estado());
        assertEquals("Carlos Gomez", resultado.nombre());
    }

    @Test
    @DisplayName("validarDisponibilidadRemota: Lanza ReglaNegocioException cuando el repartidor tiene estado INACTIVO")
    void validarDisponibilidadRemota_RepartidorInactivo_LanzaReglaNegocioException() {
        RepartidorClientResponseDto dtoInactivo = new RepartidorClientResponseDto(
                repartidorId,
                "Mariana Lopez",
                "31222333",
                "+541155443322",
                "mariana@email.com",
                "Auto",
                "INACTIVO"
        );
        when(courierClient.consultarDisponibilidad(repartidorId)).thenReturn(dtoInactivo);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                repartidorValidator.validarDisponibilidadRemota(repartidorId)
        );

        assertTrue(ex.getMessage().contains("ACTIVO"));
    }

    @Test
    @DisplayName("validarDisponibilidadRemota: Lanza IllegalArgumentException cuando courier-service retorna 404 Not Found")
    void validarDisponibilidadRemota_NotFound_LanzaIllegalArgumentException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/repartidores/" + repartidorId + "/disponibilidad",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException.NotFound notFoundException = new FeignException.NotFound("Not Found", request, null, Collections.emptyMap());

        when(courierClient.consultarDisponibilidad(repartidorId)).thenThrow(notFoundException);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                repartidorValidator.validarDisponibilidadRemota(repartidorId)
        );

        assertTrue(ex.getMessage().contains("No se encontró un repartidor"));
    }

    @Test
    @DisplayName("validarDisponibilidadRemota: Lanza ReglaNegocioException cuando courier-service retorna 400 Bad Request")
    void validarDisponibilidadRemota_BadRequest_LanzaReglaNegocioException() {
        Request request = Request.create(Request.HttpMethod.GET, "/api/v1/repartidores/" + repartidorId + "/disponibilidad",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException.BadRequest badRequestException = new FeignException.BadRequest("Bad Request", request, null, Collections.emptyMap());

        when(courierClient.consultarDisponibilidad(repartidorId)).thenThrow(badRequestException);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                repartidorValidator.validarDisponibilidadRemota(repartidorId)
        );

        assertTrue(ex.getMessage().contains("inactivo y no está disponible"));
    }

    @Test
    @DisplayName("validarDisponibilidadRemota: Lanza ReglaNegocioException cuando ocurre error de conexión o 500")
    void validarDisponibilidadRemota_ErrorConexion_LanzaReglaNegocioException() {
        when(courierClient.consultarDisponibilidad(repartidorId)).thenThrow(new RuntimeException("Connection refused"));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                repartidorValidator.validarDisponibilidadRemota(repartidorId)
        );

        assertTrue(ex.getMessage().contains("Error de comunicación"));
    }
}
