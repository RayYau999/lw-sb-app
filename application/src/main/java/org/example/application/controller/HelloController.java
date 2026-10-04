package org.example.application.controller;

import java.time.Instant;

import org.example.application.model.HelloResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public HelloResponse hello(@RequestParam(defaultValue = "World") String name) {
        return new HelloResponse("Hello, " + name + "!", "application", Instant.now());
    }
}
