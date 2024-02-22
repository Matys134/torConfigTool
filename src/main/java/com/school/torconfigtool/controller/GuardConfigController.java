package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.GuardConfigService;
import com.school.torconfigtool.model.GuardConfig;
import com.school.torconfigtool.util.RelayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> updateGuardConfiguration(@RequestBody GuardConfig config) {
        Map<String, Object> response = new HashMap<>();
        String message = guardConfigService.updateConfigAndReturnResponse(config).toString();
        if (message.equals("Guard configuration updated successfully")) {
            response.put("message", message);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", message);
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check-port-availability")
    public ResponseEntity<?> checkPortAvailability(@RequestParam String nickname, @RequestParam int orPort, @RequestParam int controlPort) {
        boolean arePortsAvailable = RelayUtils.portsAreAvailable(nickname, orPort, controlPort);

        return ResponseEntity.ok(Map.of("available", arePortsAvailable));
    }
}