package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.GuardRelayConfig;
import com.school.torconfigtool.service.GuardConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/update-guard-config")
public class GuardConfigurationController {
    private final GuardConfigurationService guardConfigurationService;

    @Autowired
    public GuardConfigurationController(GuardConfigurationService guardConfigurationService) {
        this.guardConfigurationService = guardConfigurationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> updateGuardConfiguration(@RequestBody GuardRelayConfig config) {
        Map<String, String> response = new HashMap<>();
        boolean success = guardConfigurationService.updateGuardConfiguration(config);
        if (success) {
            response.put("success", "true");
        } else {
            response.put("success", "false");
        }
        return ResponseEntity.ok(response);
    }
}
