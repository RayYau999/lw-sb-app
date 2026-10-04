package org.example.gateway.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.example.gateway.client.ApplicationClient;
import org.example.gateway.dto.HelloResponse;
import org.example.gateway.dto.Item;
import org.example.gateway.dto.ItemRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {

    private final ApplicationClient applicationClient;

    public GatewayController(ApplicationClient applicationClient) {
        this.applicationClient = applicationClient;
    }

    @GetMapping("/api/hello")
    public HelloResponse hello(@RequestParam(defaultValue = "World") String name) {
        return applicationClient.hello(name);
    }

    @GetMapping("/api/items")
    public List<Item> items() {
        return applicationClient.getItems();
    }

    @GetMapping("/api/items/{id}")
    public Item item(@PathVariable Long id) {
        return applicationClient.getItem(id);
    }

    @PostMapping("/api/items")
    public ResponseEntity<Item> createItem(@RequestBody ItemRequest request) {
        return ResponseEntity.status(201).body(applicationClient.createItem(request));
    }

    @GetMapping("/gateway/info")
    public Map<String, Object> info() {
        HelloResponse downstream = applicationClient.hello("gateway");
        return Map.of(
                "gateway", "api-gateway",
                "downstreamService", downstream.service(),
                "timestamp", Instant.now());
    }
}
