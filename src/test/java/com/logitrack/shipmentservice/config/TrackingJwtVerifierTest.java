
package com.logitrack.shipmentservice.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackingJwtVerifierTest {

    private TrackingJwtVerifier verifier;
    private SecretKey key;

    @BeforeEach
    void configurar() {
        verifier = new TrackingJwtVerifier();

        byte[] bytes = "clave-de-prueba-para-jwt-logitrack-2026"
                .getBytes(StandardCharsets.UTF_8);

        key = Keys.hmacShaKeyFor(bytes);

        ReflectionTestUtils.setField(
                verifier,
                "secretKey",
                Encoders.BASE64.encode(bytes)
        );

        ReflectionTestUtils.setField(
                verifier,
                "issuer",
                "logitrack-security"
        );
    }

    private String generarToken(String rol) {
        return Jwts.builder()
                .subject("cliente@test.com")
                .issuer("logitrack-security")
                .claim("roles", List.of(rol))
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + 60000)
                )
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Test
    void clienteConTokenValidoPuedeConsultar() {
        String token = generarToken("ROLE_CLIENTE");

        String email = verifier.exigirEmailCliente(
                "Bearer " + token
        );

        assertEquals("cliente@test.com", email);
    }

    @Test
    void usuarioAdminNoPuedeUsarEndpointDeCliente() {
        String token = generarToken("ROLE_ADMIN");

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> verifier.exigirEmailCliente(
                        "Bearer " + token
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                error.getStatusCode()
        );
    }

    @Test
    void peticionSinTokenEsRechazada() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> verifier.exigirEmailCliente(null)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                error.getStatusCode()
        );
    }
}
