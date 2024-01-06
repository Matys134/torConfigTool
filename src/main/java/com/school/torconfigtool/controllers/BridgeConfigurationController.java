package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.BridgeRelayConfig;
import com.school.torconfigtool.service.BridgeConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/update-bridge-config")
public class BridgeConfigurationController {
    private final BridgeConfigurationService bridgeConfigurationService;

    @Autowired
    public BridgeConfigurationController(BridgeConfigurationService bridgeConfigurationService) {
        this.bridgeConfigurationService = bridgeConfigurationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> updateBridgeConfiguration(@RequestBody BridgeRelayConfig config) {
        Map<String, String> response = new HashMap<>();
        boolean success = bridgeConfigurationService.updateConfiguration(config); // Corrected method name
        if (success) {
            response.put("success", "true");
        } else {
            response.put("success", "false");
        }
        return ResponseEntity.ok(response);
    }
}
