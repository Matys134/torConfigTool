package com.school.torconfigtool.controller;

import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.service.BridgeConfigService;
import com.school.torconfigtool.service.RelayUtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * This is the BridgeConfigController class. It is a REST controller that handles requests related to the bridge configuration.
 * It uses the BridgeConfigService to perform operations related to the bridge configuration.
 */
@RestController
@RequestMapping("/update-bridge-config")
public class UpdateBridgeConfigController {
    private final BridgeConfigService bridgeConfigService;

    /**
     * This is the constructor for the BridgeConfigController class.
     * It initializes the BridgeConfigService.
     * @param bridgeConfigService The service to be used for operations related to the bridge configuration.
     */
    @Autowired
    public UpdateBridgeConfigController(BridgeConfigService bridgeConfigService) {
        this.bridgeConfigService = bridgeConfigService;
    }

    /**
     * This method handles POST requests to update the bridge configuration.
     * It uses the BridgeConfigService to update the configuration and returns a response.
     * @param config The new configuration to be set.
     * @return A ResponseEntity containing a map with the response message and status.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> updateBridgeConfiguration(@RequestBody BridgeConfig config) {
        Map<String, String> response = bridgeConfigService.updateConfigAndReturnResponse(config);
        if (response.get("message").startsWith("Bridge configuration updated successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * This method handles GET requests to check the availability of ports.
     * It uses the RelayUtilityService to check if the ports are available.
     * @param nickname The nickname of the relay.
     * @param orPort The OR port to be checked.
     * @param controlPort The control port to be checked.
     * @return A ResponseEntity containing a map with the availability status of the ports.
     */
    @GetMapping("/check-port-availability")
    public ResponseEntity<?> checkPortAvailability(@RequestParam String nickname, @RequestParam int orPort, @RequestParam int controlPort) {
        boolean arePortsAvailable = RelayUtilityService.portsAreAvailable(nickname, orPort, controlPort);

        return ResponseEntity.ok(Map.of("available", arePortsAvailable));
    }
}