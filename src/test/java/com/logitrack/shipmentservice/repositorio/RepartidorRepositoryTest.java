package com.logitrack.shipmentservice.repositorio;

import com.logitrack.shipmentservice.ShipmentServiceApplication;
import com.logitrack.shipmentservice.dominio.entidad.Repartidor;
import com.logitrack.shipmentservice.dominio.enumerador.EstadoRepartidor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = ShipmentServiceApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepartidorRepositoryTest {

    @Autowired
    private RepartidorRepository repartidorRepository;

    @Test
    @DisplayName("save: Persiste un repartidor y genera su UUID automáticamente")
    void save_PersisteRepartidor() {
        Repartidor repartidor = Repartidor.builder()
                .nombre("Martin Palermo")
                .dni("20111222")
                .telefono("+541122334455")
                .tipoVehiculo("Camioneta")
                .estado(EstadoRepartidor.ACTIVO)
                .build();

        Repartidor guardado = repartidorRepository.save(repartidor);

        assertNotNull(guardado.getId());
        assertEquals("Martin Palermo", guardado.getNombre());
        assertEquals("20111222", guardado.getDni());
        assertEquals("+541122334455", guardado.getTelefono());
        assertEquals(EstadoRepartidor.ACTIVO, guardado.getEstado());
    }

    @Test
    @DisplayName("findByEstado: Retorna solo los repartidores que coinciden con el estado y respeta paginación")
    void findByEstado_FiltraPorEstado() {
        Repartidor activo1 = Repartidor.builder()
                .nombre("Repartidor Activo 1")
                .dni("21111222")
                .telefono("+541122334456")
                .tipoVehiculo("Moto")
                .estado(EstadoRepartidor.ACTIVO)
                .build();
        Repartidor activo2 = Repartidor.builder()
                .nombre("Repartidor Activo 2")
                .dni("22111222")
                .telefono("+541122334457")
                .tipoVehiculo("Auto")
                .estado(EstadoRepartidor.ACTIVO)
                .build();
        Repartidor inactivo = Repartidor.builder()
                .nombre("Repartidor Inactivo")
                .dni("23111222")
                .telefono("+541122334458")
                .tipoVehiculo("Bicicleta")
                .estado(EstadoRepartidor.INACTIVO)
                .build();

        repartidorRepository.save(activo1);
        repartidorRepository.save(activo2);
        repartidorRepository.save(inactivo);

        Page<Repartidor> paginaActivos = repartidorRepository.findByEstado(
                EstadoRepartidor.ACTIVO,
                PageRequest.of(0, 10)
        );

        assertEquals(2, paginaActivos.getTotalElements());
        assertTrue(paginaActivos.getContent().stream()
                .allMatch(r -> r.getEstado() == EstadoRepartidor.ACTIVO));
    }

    @Test
    @DisplayName("delete: Aplica baja lógica (@SoftDelete) ocultando el registro en consultas posteriores")
    void delete_AplicaSoftDelete() {
        Repartidor repartidor = Repartidor.builder()
                .nombre("Juan Román")
                .dni("24111222")
                .telefono("+541122334459")
                .tipoVehiculo("Moto")
                .estado(EstadoRepartidor.ACTIVO)
                .build();

        Repartidor guardado = repartidorRepository.save(repartidor);
        UUID id = guardado.getId();
        assertNotNull(id);

        // Ejecutar eliminación
        repartidorRepository.delete(guardado);
        repartidorRepository.flush();

        // Verificar que ya no se encuentra en findById debido al @SoftDelete
        Optional<Repartidor> encontrado = repartidorRepository.findById(id);
        assertTrue(encontrado.isEmpty());
    }
}
