package com.logitrack.shipmentservice.repositorio;

import com.logitrack.shipmentservice.ShipmentServiceApplication;
import com.logitrack.shipmentservice.dominio.entidad.Cliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración de persistencia para ClienteRepository.
 * Verifica los métodos de consulta por DNI, Email, validaciones de existencia (incluyendo exclusión por ID)
 * y el funcionamiento del soft delete automático de Hibernate.
 *
 * @author LuisMPlus, 95670446-maker y leoM2022 / Equipo lazyHawk
 * @version 1.0.0
 */
@DataJpaTest
@ContextConfiguration(classes = ShipmentServiceApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    @DisplayName("save: Persiste un cliente y autogenera su UUID y auditoría")
    void save_PersisteCliente() {
        Cliente cliente = Cliente.builder()
                .nombre("Lionel Messi")
                .dni("10101010")
                .telefono("+541122334455")
                .email("lionel.messi@email.com")
                .build();

        Cliente guardado = clienteRepository.save(cliente);

        assertNotNull(guardado.getId());
        assertEquals("Lionel Messi", guardado.getNombre());
        assertEquals("10101010", guardado.getDni());
        assertEquals("+541122334455", guardado.getTelefono());
        assertEquals("lionel.messi@email.com", guardado.getEmail());
    }

    @Test
    @DisplayName("findByDni y findByEmail: Encuentra clientes por sus identificadores únicos")
    void findByDniYFindByEmail_Exitoso() {
        Cliente cliente = Cliente.builder()
                .nombre("Ángel Di María")
                .dni("11112222")
                .telefono("+541133445566")
                .email("angel.dimaria@email.com")
                .build();
        clienteRepository.save(cliente);

        Optional<Cliente> porDni = clienteRepository.findByDni("11112222");
        Optional<Cliente> porEmail = clienteRepository.findByEmail("angel.dimaria@email.com");

        assertTrue(porDni.isPresent());
        assertEquals("Ángel Di María", porDni.get().getNombre());
        assertTrue(porEmail.isPresent());
        assertEquals("Ángel Di María", porEmail.get().getNombre());
    }

    @Test
    @DisplayName("existsByDni y existsByEmail: Comprueba existencia correctamente")
    void existsByDniYExistsByEmail_VerificaExistencia() {
        Cliente cliente = Cliente.builder()
                .nombre("Rodrigo De Paul")
                .dni("12121212")
                .telefono("+541144556677")
                .email("rodrigo.depaul@email.com")
                .build();
        clienteRepository.save(cliente);

        assertTrue(clienteRepository.existsByDni("12121212"));
        assertFalse(clienteRepository.existsByDni("99999999"));

        assertTrue(clienteRepository.existsByEmail("rodrigo.depaul@email.com"));
        assertFalse(clienteRepository.existsByEmail("otro@email.com"));
    }

    @Test
    @DisplayName("existsByDniAndIdNot y existsByEmailAndIdNot: Detecta colisiones al editar excluyendo al propio cliente")
    void existsConExclusionDeId_DetectaColisiones() {
        Cliente cliente1 = clienteRepository.save(Cliente.builder()
                .nombre("Cliente Uno")
                .dni("13131313")
                .telefono("+541155667788")
                .email("uno@email.com")
                .build());

        Cliente cliente2 = clienteRepository.save(Cliente.builder()
                .nombre("Cliente Dos")
                .dni("14141414")
                .telefono("+541166778899")
                .email("dos@email.com")
                .build());

        // Si cliente1 mantiene su propio DNI/Email, no debe considerarse duplicado
        assertFalse(clienteRepository.existsByDniAndIdNot("13131313", cliente1.getId()));
        assertFalse(clienteRepository.existsByEmailAndIdNot("uno@email.com", cliente1.getId()));

        // Si cliente1 intenta usar el DNI/Email de cliente2, debe detectar colisión
        assertTrue(clienteRepository.existsByDniAndIdNot("14141414", cliente1.getId()));
        assertTrue(clienteRepository.existsByEmailAndIdNot("dos@email.com", cliente1.getId()));
    }

    @Test
    @DisplayName("delete: Aplica soft delete (@SoftDelete) ocultando el registro en consultas posteriores")
    void delete_AplicaSoftDelete() {
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nombre("Julián Álvarez")
                .dni("15151515")
                .telefono("+541177889900")
                .email("julian.alvarez@email.com")
                .build());

        UUID id = cliente.getId();
        assertNotNull(id);

        clienteRepository.delete(cliente);
        clienteRepository.flush();

        Optional<Cliente> resultado = clienteRepository.findById(id);
        assertTrue(resultado.isEmpty());
    }
}
