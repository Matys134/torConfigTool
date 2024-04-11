package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.OnionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * OnionRestController is a Spring REST Controller that handles operations related to Onion Services.
 */
@RestController
@RequestMapping("/onion-api")
public class OnionRestController {
    private final OnionService onionService;

    /**
     * Constructor for OnionRestController.
     * @param onionService The service to handle onion operations.
     */
    @Autowired
    public OnionRestController(OnionService onionService) {
        this.onionService = onionService;
    }

    /**
     * Handles the GET request to check if the Onion Service is configured.
     * @return ResponseEntity<Map<String, Boolean>> Returns a ResponseEntity with the status of the Onion Service configuration.
     */
    @GetMapping("/onion-configured")
    public ResponseEntity<Map<String, Boolean>> checkOnionConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        boolean isOnionConfigured = onionService.checkOnionConfigured();
        response.put("onionConfigured", isOnionConfigured);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the GET request to get the current hostnames.
     * @return Map<String, String> Returns a map of the current hostnames.
     */
    @GetMapping("/current-hostnames")
    public Map<String, String> getCurrentHostnames() {
        return onionService.getCurrentHostnames();
    }
}