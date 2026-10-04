package org.example.gateway.client;

import java.util.List;

import org.example.gateway.dto.HelloResponse;
import org.example.gateway.dto.Item;
import org.example.gateway.dto.ItemRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "application-service", url = "${application.service.base-url}")
public interface ApplicationClient {

    @GetMapping("/api/hello")
    HelloResponse hello(@RequestParam("name") String name);

    @GetMapping("/api/items")
    List<Item> getItems();

    @GetMapping("/api/items/{id}")
    Item getItem(@PathVariable("id") Long id);

    @PostMapping("/api/items")
    Item createItem(@RequestBody ItemRequest request);
}
