package org.example.gateway.dto;

import java.time.Instant;

public record HelloResponse(String message, String service, Instant timestamp) {
}
