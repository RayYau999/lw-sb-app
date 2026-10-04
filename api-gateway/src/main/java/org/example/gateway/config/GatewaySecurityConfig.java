package org.example.gateway.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.example.gateway.security.JwtAuthenticationFailureHandler;
import org.example.gateway.security.JwtIssuingSuccessHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtIssuingSuccessHandler successHandler,
                                                   JwtAuthenticationFailureHandler failureHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(login -> login
                        .successHandler(successHandler)
                        .failureHandler(failureHandler));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.auth.jwt-secret}") String jwtSecret) {
        return NimbusJwtDecoder.withSecretKey(hmacKey(jwtSecret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(@Value("${app.auth.jwt-secret}") String jwtSecret) {
        return NimbusJwtEncoder.withSecretKey(hmacKey(jwtSecret))
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    private static SecretKey hmacKey(String base64Secret) {
        byte[] key;
        try {
            key = Base64.getDecoder().decode(base64Secret.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("app.auth.jwt-secret must be a valid base64 encoded key", ex);
        }
        if (key.length < 32) {
            throw new IllegalStateException("app.auth.jwt-secret must decode to at least 32 bytes for HS256");
        }
        return new SecretKeySpec(key, "HmacSHA256");
    }
}
