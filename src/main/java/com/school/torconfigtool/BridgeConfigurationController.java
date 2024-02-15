package com.school.torconfigtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a REST controller that handles HTTP requests related to bridge configuration.
 * It uses the BridgeConfigurationService to perform the actual operations.
 */
@RestController
@RequestMapping("/update-bridge-config")
public class BridgeConfigurationController {
    private final BridgeConfigurationService bridgeConfigurationService;

    /**
     * This constructor is used to inject the BridgeConfigurationService into this controller.
     * @param bridgeConfigurationService The service that will be used to handle bridge configuration operations.
     */
    @Autowired
    public BridgeConfigurationController(BridgeConfigurationService bridgeConfigurationService) {
        this.bridgeConfigurationService = bridgeConfigurationService;
    }

    /**
     * This method handles POST requests to update the bridge configuration.
     * It uses the BridgeConfigurationService to perform the update operation.
     * @param config The new configuration for the bridge relay.
     * @return A ResponseEntity containing a map with a single entry. The key is "success" and the value is "true" if the update was successful, "false" otherwise.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> updateBridgeConfiguration(@RequestBody BridgeRelayConfig config) {
        Map<String, String> response = new HashMap<>();
        boolean success = bridgeConfigurationService.updateConfiguration(config); // Corrected method name
        if (success) {
            response.put("success", "true");
        } else {
            response.put("success", "false");
        }
        return ResponseEntity.ok(response);
    }
}