package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.UpdateGuardConfigService;
import com.school.torconfigtool.model.GuardConfig;
import com.school.torconfigtool.service.RelayUtilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * GuardConfigController is a Spring MVC RestController that handles HTTP requests related to updating the GuardConfig.
 * It uses GuardConfigService to perform the actual operations.
 */
@RestController
@RequestMapping("/update-guard-config")
public class UpdateGuardConfigRestController {

    private final UpdateGuardConfigService updateGuardConfigService;

    /**
     * Constructs a new GuardConfigController with the provided GuardConfigService.
     *
     * @param updateGuardConfigService the GuardConfigService to be used for GuardConfig operations
     */
    public UpdateGuardConfigRestController(UpdateGuardConfigService updateGuardConfigService) {
        this.updateGuardConfigService = updateGuardConfigService;
    }

    /**
     * Handles POST requests to update the GuardConfig.
     *
     * @param config the GuardConfig to be updated
     * @return a ResponseEntity that contains the result of the update operation
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> updateGuardConfiguration(@RequestBody GuardConfig config) {
        Map<String, String> response = updateGuardConfigService.updateConfiguration(config);
        if (response.get("message").startsWith("Guard configuration updated successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Handles GET requests to check the availability of ports.
     *
     * @param nickname     the nickname of the relay
     * @param orPort       the OR port to be checked
     * @param controlPort  the control port to be checked
     * @return a ResponseEntity that contains the availability status of the ports
     */
    @GetMapping("/check-port-availability")
    public ResponseEntity<?> checkPortAvailability(@RequestParam String nickname, @RequestParam int orPort, @RequestParam int controlPort) {
        boolean arePortsAvailable = RelayUtilityService.portsAreAvailable(nickname, orPort, controlPort);

        return ResponseEntity.ok(Map.of("available", arePortsAvailable));
    }
}