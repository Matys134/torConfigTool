package com.school.torconfigtool.controller;

import com.school.torconfigtool.model.GuardConfig;
import com.school.torconfigtool.service.GuardConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/update-guard-config")
public class GuardConfigController {

    private final GuardConfigService guardConfigService;
    private static final Logger logger = LoggerFactory.getLogger(GuardConfigController.class);

    public GuardConfigController(GuardConfigService guardConfigService) {
        this.guardConfigService = guardConfigService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> updateGuardConfiguration(@RequestBody GuardConfig config) {
        logger.info("Received request to update guard configuration: {}", config);
        Map<String, String> response = guardConfigService.updateConfigAndReturnResponse(config);
        logger.info("Response from guard configuration update: {}", response);
        if (response.get("message").equals("Guard configuration updated successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}