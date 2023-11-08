package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.GuardRelayConfig;
import com.school.torconfigtool.service.GuardConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/update-guard-config")
public class GuardConfigurationController {
    private static final Logger logger = LoggerFactory.getLogger(GuardConfigurationController.class);
    private final GuardConfigurationService guardConfigurationService;

    public GuardConfigurationController(GuardConfigurationService guardConfigurationService) {
        this.guardConfigurationService = guardConfigurationService;
    }

    @PostMapping
    public ResponseEntity<?> updateGuardConfiguration(@RequestBody GuardRelayConfig config) {
        try {
            boolean success = guardConfigurationService.updateConfiguration(config);
            if (success) {
                logger.info("Guard configuration updated successfully for relay: {}", config.getNickname());
                return ResponseEntity.ok(Map.of("success", "Guard configuration updated successfully"));
            } else {
                logger.warn("Failed to update guard configuration for relay: {}", config.getNickname());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Failed to update guard configuration"));
            }
        } catch (Exception e) {
            logger.error("Exception occurred while updating guard configuration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred"));
        }
    }
}
