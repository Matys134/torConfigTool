package com.school.torconfigtool.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a REST controller that handles requests to update the configuration of a bridge.
 * It uses the BridgeConfigurationService to perform the actual update.
 */
@RestController
@RequestMapping("/update-bridge-config")
public class BridgeConfigurationController {
    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigurationController.class);
    private final BridgeConfigurationService bridgeConfigurationService;

    /**
     * Constructor for the BridgeConfigurationController.
     * @param bridgeConfigurationService the service used to update the bridge configuration
     */
    @Autowired
    public BridgeConfigurationController(BridgeConfigurationService bridgeConfigurationService) {
        this.bridgeConfigurationService = bridgeConfigurationService;
    }

    /**
     * This method handles POST requests to update the bridge configuration.
     * It uses the BridgeConfigurationService to perform the update.
     * If the update is successful, it returns a 200 OK response with a success message.
     * If the update fails, it returns a 500 Internal Server Error response with an error message.
     * @param config the new configuration for the bridge
     * @return a ResponseEntity with the status and a message indicating the result of the operation
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> updateBridgeConfiguration(@RequestBody BridgeConfig config) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = bridgeConfigurationService.updateConfiguration(config);
            if (success) {
                logger.info("Bridge configuration updated successfully for relay: {}", config.getNickname());
                response.put("message", "Bridge configuration updated successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Failed to update bridge configuration for relay: {}", config.getNickname());
                response.put("message", "Failed to update bridge configuration");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            logger.error("Exception occurred while updating bridge configuration", e);
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}