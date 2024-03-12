package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.RelayInformationService;
import com.school.torconfigtool.service.SetupService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/setup")
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
    @GetMapping
    public String setup() {
        // Add any necessary data to the model
        return "setup"; // "setup" corresponds to your Thymeleaf template file
    }

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
