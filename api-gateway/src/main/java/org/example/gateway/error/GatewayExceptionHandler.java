package org.example.gateway.error;

import java.util.Map;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(FeignException ex) {
        boolean downstreamResponded = ex.status() > 0;
        int status = downstreamResponded ? ex.status() : 503;
        String reason = downstreamResponded
                ? HttpStatus.resolve(status).getReasonPhrase()
                : HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase();
        String message = downstreamResponded
                ? "Application service returned status " + ex.status()
                : "Application service is unreachable";

        return ResponseEntity.status(status)
                .body(Map.of("status", status, "error", reason, "message", message));
    }
}
