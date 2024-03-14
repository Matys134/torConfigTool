package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.RelayInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is a REST controller that handles operations related to the setup of the application.
 * It provides endpoints for getting the limit state and guard count, toggling the limit on the number of bridges that can be configured, and getting the state of the bridge limit.
 */
@RestController
@RequestMapping("/setup-api")
public class SetupRestController {


    @GetMapping("/limit-state")
    public ResponseEntity<Boolean> getLimitState() {
        return ResponseEntity.ok(RelayInformationService.isLimitOn());
    }

    @PostMapping("/toggle-limit")
    public ResponseEntity<Void> toggleLimit() {
        RelayInformationService.toggleLimit();
        return ResponseEntity.ok().build();
    }
}