package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.GuardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * This is a Rest Controller class for handling API requests related to Guard.
 * It is mapped to "/guard-api" path.
 */
@RestController
@RequestMapping("/guard-api")
public class GuardRestController {

    // GuardService instance used to perform operations related to Guard.
    private final GuardService guardService;

    /**
     * Constructor for GuardApiController.
     * @param guardService - GuardService instance is Autowired (injected automatically by Spring).
     */
    @Autowired
    public GuardRestController(GuardService guardService) {
        this.guardService = guardService;
    }

    /**
     * This method is mapped to the "/guards/limit-reached" path.
     * It checks if the limit for the Guard has been reached.
     * @return ResponseEntity - It returns a Map of String and Object wrapped in a ResponseEntity with an HTTP status code.
     */
    @GetMapping("/guards/limit-reached")
    public ResponseEntity<Map<String, Object>> checkGuardLimit() {
        return ResponseEntity.ok(guardService.countGuards());
    }

    /**
     * This method is mapped to the "/guard-configured" path.
     * It checks if the Guard has been configured.
     * @return ResponseEntity - It returns a Map of String and Boolean wrapped in a ResponseEntity with an HTTP status code.
     */
    @GetMapping("/guard-configured")
    public ResponseEntity<Map<String, Boolean>> checkGuardConfigured() {
        return ResponseEntity.ok(guardService.checkGuardConfigured());
    }
}