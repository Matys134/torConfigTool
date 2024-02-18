package com.school.torconfigtool.service;

import com.school.torconfigtool.BridgeConfigurationService;
import com.school.torconfigtool.model.BridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a service class for handling the business logic of updating the bridge configuration.
 * It uses the BridgeConfigurationService to perform the actual update operation.
 */
@Service
public class BridgeConfigService {
    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigService.class);
    private final BridgeConfigurationService bridgeConfigurationService;

    /**
     * Constructor for the BridgeConfigService.
     * It takes a BridgeConfigurationService as a parameter and assigns it to the bridgeConfigurationService field.
     *
     * @param bridgeConfigurationService the service class that performs the actual update operation
     */
    public BridgeConfigService(BridgeConfigurationService bridgeConfigurationService) {
        this.bridgeConfigurationService = bridgeConfigurationService;
    }

    /**
     * This method updates the bridge configuration.
     * It takes a BridgeConfig object as a parameter and uses the BridgeConfigurationService to update the configuration.
     * If the update is successful, it logs a success message and returns a map with a success message.
     * If the update fails, it logs a warning and returns a map with an error message.
     * If an exception occurs during the update operation, it logs the exception and returns a map with an error message.
     *
     * @param config the BridgeConfig object that contains the new configuration
     * @return a map that contains a message indicating the result of the operation
     */
    public Map<String, String> updateBridgeConfig(BridgeConfig config) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = bridgeConfigurationService.updateConfiguration(config);
            if (success) {
                logger.info("Bridge configuration updated successfully for relay: {}", config.getNickname());
                response.put("message", "Bridge configuration updated successfully");
            } else {
                logger.warn("Failed to update bridge configuration for relay: {}", config.getNickname());
                response.put("message", "Failed to update bridge configuration");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while updating bridge configuration", e);
            response.put("message", "An unexpected error occurred");
        }
        return response;
    }
}