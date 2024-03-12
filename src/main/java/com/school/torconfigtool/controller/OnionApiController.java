package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.OnionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/onion-api")
public class OnionApiController {
    private final OnionService onionService;

    @Autowired
    public OnionApiController(OnionService onionService) {
        this.onionService = onionService;
    }

    @GetMapping("/onion-configured")
    public ResponseEntity<Map<String, Boolean>> checkOnionConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        boolean isOnionConfigured = onionService.checkOnionConfigured();
        response.put("onionConfigured", isOnionConfigured);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current-hostnames")
    public Map<String, String> getCurrentHostnames() {
        return onionService.getCurrentHostnames();
    }
}
