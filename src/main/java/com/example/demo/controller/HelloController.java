package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostname = "unknown";
        }
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Hello from Spring Boot on K8s!");
        result.put("hostname", hostname);
        result.put("time", LocalDateTime.now().toString());
        return result;
    }

    @GetMapping("/new-hello")
    public Map<String, Object> newHello() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "newHelloWorld");
        result.put("time", LocalDateTime.now().toString());
        return result;
    }
}
