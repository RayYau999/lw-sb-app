package org.example.gateway.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JwtDecoder jwtDecoder;

    public AuthController(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @GetMapping("/api/auth/login")
    public ResponseEntity<Void> login() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/google")
                .build();
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<Map<String, Object>> me(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized();
        }
        try {
            Jwt jwt = jwtDecoder.decode(authorization.substring("Bearer ".length()));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("sub", jwt.getSubject());
            body.put("email", jwt.getClaimAsString("email"));
            body.put("name", jwt.getClaimAsString("name"));
            body.put("picture", jwt.getClaimAsString("picture"));
            body.put("iss", jwt.getClaimAsString("iss"));
            body.put("exp", jwt.getExpiresAt() != null ? jwt.getExpiresAt().toString() : null);
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            log.debug("Bearer token validation failed", ex);
            return unauthorized();
        }
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<Map<String, Object>> unauthorized() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message", "Missing or invalid access token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
}
