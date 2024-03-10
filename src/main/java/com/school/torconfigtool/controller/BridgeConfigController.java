package com.school.torconfigtool.controller;

import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.service.BridgeConfigService;
import com.school.torconfigtool.service.RelayUtilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * This is a REST controller for updating the bridge configuration.
 * It uses the BridgeConfigService to handle the business logic.
 */
@RestController
@RequestMapping("/update-bridge-config")
public class BridgeConfigController {
    private final BridgeConfigService bridgeConfigService;
    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigController.class);

    /**
     * Constructor for the BridgeConfigController.
     * It takes a BridgeConfigService as a parameter and assigns it to the bridgeConfigService field.
     *
     * @param bridgeConfigService the service class that handles the business logic for updating the bridge configuration
     */
    @Autowired
    public BridgeConfigController(BridgeConfigService bridgeConfigService) {
        this.bridgeConfigService = bridgeConfigService;
    }

    /**
     * This method is a POST endpoint for updating the bridge configuration.
     * It takes a BridgeConfig object as a request body and uses the BridgeConfigService to update the configuration.
     * If the update is successful, it returns a 200 OK response with a success message.
     * If the update fails, it returns a 500 Internal Server Error response with an error message.
     *
     * @param config the BridgeConfig object that contains the new configuration
     * @return a ResponseEntity that contains the status code and a message indicating the result of the operation
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> updateBridgeConfiguration(@RequestBody BridgeConfig config) {
        logger.info("Received request to update bridge configuration: {}", config);
        Map<String, String> response = bridgeConfigService.updateConfigAndReturnResponse(config);
        logger.info("Response from bridge configuration update: {}", response);
        if (response.get("message").startsWith("Bridge configuration updated successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check-port-availability")
    public ResponseEntity<?> checkPortAvailability(@RequestParam String nickname, @RequestParam int orPort, @RequestParam int controlPort) {
        boolean arePortsAvailable = RelayUtilityService.portsAreAvailable(nickname, orPort, controlPort);

        return ResponseEntity.ok(Map.of("available", arePortsAvailable));
    }
}