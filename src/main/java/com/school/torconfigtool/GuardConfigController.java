package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/update-guard-config")
public class GuardConfigController {
    private static final Logger logger = LoggerFactory.getLogger(GuardConfigController.class);
    private final GuardConfigService guardConfigService;

    public GuardConfigController(GuardConfigService guardConfigService) {
        this.guardConfigService = guardConfigService;
    }

    @PostMapping
    public ResponseEntity<?> updateGuardConfiguration(@RequestBody GuardConfig config) {
        try {
            boolean success = guardConfigService.updateConfiguration(config);
            if (success) {
                logger.info("Guard configuration updated successfully for relay: {}", config.getNickname());
                return ResponseEntity.ok(Map.of("success", "Guard configuration updated successfully"));
            } else {
                logger.warn("Failed to update guard configuration for relay: {}", config.getNickname());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update guard configuration"));
            }
        } catch (Exception e) {
            logger.error("Exception occurred while updating guard configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred"));
        }
    }
}
