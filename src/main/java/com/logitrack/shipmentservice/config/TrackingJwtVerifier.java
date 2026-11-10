package com.logitrack.shipmentservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.List;

@Component
public class TrackingJwtVerifier {

    @Value("${security.jwt.secret-key:}")
    private String secretKey;

    @Value("${security.jwt.issuer:logitrack-security}")
    private String issuer;

    public String exigirEmailCliente(String authorization) {

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Se requiere un token JWT"
            );
        }

        if (secretKey == null || secretKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "La validación JWT no está configurada"
            );
        }

        Claims claims;

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(
                            authorization.substring(7)
                    )
                    .getPayload();

        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token inválido o expirado"
            );
        }

        if (claims.getExpiration() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token no tiene expiración"
            );
        }

        Object rolesClaim = claims.get("roles");

        List<String> roles;

        if (rolesClaim instanceof List<?> lista) {
            roles = lista.stream()
                    .map(Object::toString)
                    .toList();
        } else if (rolesClaim instanceof String rol) {
            roles = List.of(rol);
        } else {
            roles = List.of();
        }

        boolean esCliente = roles.stream().anyMatch(
                rol -> rol.equalsIgnoreCase("ROLE_CLIENTE")
                        || rol.equalsIgnoreCase("CLIENTE")
        );

        if (!esCliente) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Se requiere el rol CLIENTE"
            );
        }

        String email = claims.getSubject();

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token no identifica al usuario"
            );
        }

        return email.trim();
    }
}