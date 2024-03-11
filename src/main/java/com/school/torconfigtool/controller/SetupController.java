package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.SetupService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class SetupController {

    private final SetupService setupService;

    public SetupController(SetupService setupService) {
        this.setupService = setupService;
    }

    /**
     * Handles GET requests to the "/setup" endpoint.
     * Returns the setup view.
     *
     * @return the name of the setup view
     */
    @GetMapping("/setup")
    public String setup() {
        // Add any necessary data to the model
        return "setup"; // "setup" corresponds to your Thymeleaf template file
    }

    @GetMapping("/limit-state-and-count")
    public ResponseEntity<Map<String, Object>> getLimitStateAndGuardCount() {
        return ResponseEntity.ok(setupService.getLimitStateAndCount());
    }
}
