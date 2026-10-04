package org.example.application.model;

import java.time.Instant;

public record HelloResponse(String message, String service, Instant timestamp) {
}
