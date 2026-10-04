package org.example.gateway.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class GoogleClientRegistrationConfig {

    public static final String REGISTRATION_ID = "google";
    public static final String USER_INFO_URI = "https://openidconnect.googleapis.com/v1/userinfo";
    public static final String JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${app.google.client-secret-file:}") String clientSecretFile,
            @Value("${app.auth.base-url:http://localhost:8080}") String baseUrl) throws IOException {

        Path file = resolveClientSecretFile(clientSecretFile);
        JsonNode web;
        try (InputStream in = Files.newInputStream(file)) {
            web = new ObjectMapper().readTree(in).path("web");
        }

        String clientId = web.path("client_id").asString("");
        String clientSecret = web.path("client_secret").asString("");
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException(
                    "Missing web.client_id or web.client_secret in " + file.toAbsolutePath());
        }

        ClientRegistration registration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(baseUrl + "/login/oauth2/code/" + REGISTRATION_ID)
                .scope("openid", "email", "profile")
                .authorizationUri(web.path("auth_uri").asString("https://accounts.google.com/o/oauth2/auth"))
                .tokenUri(web.path("token_uri").asString("https://oauth2.googleapis.com/token"))
                .userInfoUri(USER_INFO_URI)
                .jwkSetUri(JWK_SET_URI)
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }

    private Path resolveClientSecretFile(String configured) throws IOException {
        if (configured != null && !configured.isBlank()) {
            Path path = Path.of(configured);
            if (!Files.isRegularFile(path)) {
                throw new IllegalStateException("Google client secret file not found: " + path.toAbsolutePath());
            }
            return path;
        }

        Path dir = Path.of("secret");
        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException(
                    "Directory secret/ not found. Place your Google client_secret_*.json there "
                            + "or set app.google.client-secret-file.");
        }
        List<Path> matches;
        try (Stream<Path> files = Files.list(dir)) {
            matches = files.filter(p -> p.getFileName().toString().startsWith("client_secret"))
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .toList();
        }
        if (matches.isEmpty()) {
            throw new IllegalStateException(
                    "No client_secret_*.json found in secret/. "
                            + "Set app.google.client-secret-file to point at your Google OAuth client secret.");
        }
        return matches.get(0);
    }
}
