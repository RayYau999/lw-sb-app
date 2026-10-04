package org.example.gateway.security;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtIssuingSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtEncoder jwtEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String issuer;
    private final long ttlSeconds;

    public JwtIssuingSuccessHandler(JwtEncoder jwtEncoder,
                                    @Value("${app.auth.issuer:lw-sb-app}") String issuer,
                                    @Value("${app.auth.ttl-seconds:3600}") long ttlSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User principal = ((OAuth2AuthenticationToken) authentication).getPrincipal();

        String sub = firstNonNull(principal.getAttribute("sub"),
                principal.getAttribute("email"),
                "unknown");
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        String picture = principal.getAttribute("picture");

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(sub)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .id(UUID.randomUUID().toString())
                .claims(claimMap -> {
                    putIfPresent(claimMap, "email", email);
                    putIfPresent(claimMap, "name", name);
                    putIfPresent(claimMap, "picture", picture);
                })
                .build();

        String accessToken = jwtEncoder.encode(
                        JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("sub", sub);
        putIfPresent(user, "email", email);
        putIfPresent(user, "name", name);
        putIfPresent(user, "picture", picture);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("access_token", accessToken);
        body.put("token_type", "Bearer");
        body.put("expires_in", ttlSeconds);
        body.put("user", user);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private static void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private static String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
