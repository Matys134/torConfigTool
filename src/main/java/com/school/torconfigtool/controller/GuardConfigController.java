package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.GuardConfigService;
import com.school.torconfigtool.model.GuardConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GuardConfigController is a Spring MVC RestController that handles HTTP requests related to updating the GuardConfig.
 * It uses GuardConfigService to perform the actual operations.
 */
@RestController
@RequestMapping("/update-guard-config")
public class GuardConfigController {

    private final GuardConfigService guardConfigService;

    /**
     * Constructs a new GuardConfigController with the provided GuardConfigService.
     *
     * @param guardConfigService the GuardConfigService to be used for GuardConfig operations
     */
    public GuardConfigController(GuardConfigService guardConfigService) {
        this.guardConfigService = guardConfigService;
    }

    /**
     * Handles POST requests to update the GuardConfig.
     *
     * @param config the GuardConfig to be updated
     * @return a ResponseEntity that contains the result of the update operation
     */
    @PostMapping
    public ResponseEntity<?> updateGuardConfiguration(@RequestBody GuardConfig config) {
        return guardConfigService.updateGuardConfiguration(config);
    }
}