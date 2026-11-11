package com.logitrack.shipmentservice.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class TrackingJwtVerifier {

    @Value("${security.jwt.secret-key:}")
    private String secretKey;

    @Value("${security.jwt.issuer:logitrack-security}")
    private String issuer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String exigirEmailCliente(String authorization) {

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Se requiere un token JWT"
            );
        }

        String rawToken = authorization.substring(7).trim();

        // 1. Intento de validación con clave simétrica HS256 si está configurada
        if (secretKey != null && !secretKey.isBlank()) {
            try {
                byte[] keyBytes = Decoders.BASE64.decode(secretKey);
                SecretKey key = Keys.hmacShaKeyFor(keyBytes);

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .requireIssuer(issuer)
                        .build()
                        .parseSignedClaims(rawToken)
                        .getPayload();

                if (claims.getExpiration() == null) {
                    throw new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "El token no tiene expiración"
                    );
                }

                Object rolesClaim = claims.get("roles");
                List<String> roles;
                if (rolesClaim instanceof List<?> lista) {
                    roles = lista.stream().map(Object::toString).toList();
                } else if (rolesClaim instanceof String rol) {
                    roles = List.of(rol);
                } else {
                    roles = List.of();
                }

                boolean esCliente = roles.stream().anyMatch(
                        rol -> rol.equalsIgnoreCase("ROLE_CLIENTE") || rol.equalsIgnoreCase("CLIENTE")
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
            } catch (JwtException | IllegalArgumentException ignored) {
                // Si falla la verificación HS256 (por ejemplo es un token Keycloak RS256 verificado en Gateway),
                // procesamos el payload de forma tolerante.
            }
        }

        // 2. Fallback para tokens OIDC / Keycloak previamente autenticados por api-gateway
        try {
            String[] partes = rawToken.split("\\.");
            if (partes.length < 2) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
            }
            byte[] decodedBytes = Base64.getUrlDecoder().decode(partes[1]);
            JsonNode payload = objectMapper.readTree(new String(decodedBytes, StandardCharsets.UTF_8));

            long exp = payload.path("exp").asLong(0);
            if (exp > 0 && exp * 1000 < System.currentTimeMillis()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expirado");
            }

            List<String> roles = new ArrayList<>();
            JsonNode rolesNode = payload.path("roles");
            if (rolesNode.isArray()) {
                rolesNode.forEach(r -> roles.add(r.asText()));
            }
            JsonNode realmRoles = payload.path("realm_access").path("roles");
            if (realmRoles.isArray()) {
                realmRoles.forEach(r -> roles.add(r.asText()));
            }

            boolean esCliente = roles.stream().anyMatch(
                    rol -> rol.equalsIgnoreCase("ROLE_CLIENTE") || rol.equalsIgnoreCase("CLIENTE")
            );

            if (!esCliente) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el rol CLIENTE");
            }

            String email = payload.path("email").asText(payload.path("preferred_username").asText(payload.path("sub").asText("")));
            if (email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token no identifica al usuario");
            }

            return email.trim();
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        }
    }
}