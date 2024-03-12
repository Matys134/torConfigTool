package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.RelayInformationService;
import com.school.torconfigtool.service.SetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * This is a REST controller that handles operations related to the setup of the application.
 * It provides endpoints for getting the limit state and guard count, toggling the limit on the number of bridges that can be configured, and getting the state of the bridge limit.
 */
@RestController
@RequestMapping("/setup-api")
public class SetupApiController {

    private final SetupService setupService;

    /**
     * Constructor for SetupApiController.
     * @param setupService The service for setup operations.
     */
    @Autowired
    public SetupApiController(SetupService setupService) {
        this.setupService = setupService;
    }

    /**
     * Endpoint to get the limit state and guard count.
     * @return The limit state and guard count.
     */
    @GetMapping("/limit-state-and-count")
    public ResponseEntity<Map<String, Object>> getLimitStateAndGuardCount() {
        return ResponseEntity.ok(setupService.getLimitStateAndCount());
    }

    /**
     * Endpoint for toggling the limit on the number of bridges that can be configured.
     * @return ResponseEntity<Void> - The response entity indicating the result of the operation.
     */
    @PostMapping("/toggle-limit")
    public ResponseEntity<Void> toggleLimit() {
        RelayInformationService.toggleLimit();
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for getting the state of the bridge limit.
     * @return ResponseEntity<Boolean> - The response entity containing the state of the bridge limit.
     */
    @GetMapping("/limit-state")
    public ResponseEntity<Boolean> getLimitState() {
        return ResponseEntity.ok(RelayInformationService.isLimitOn());
    }
}