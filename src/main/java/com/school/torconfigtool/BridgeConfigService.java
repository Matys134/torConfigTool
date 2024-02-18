package com.school.torconfigtool;

import com.school.torconfigtool.model.BridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BridgeConfigService {
    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigService.class);
    private final BridgeConfigurationService bridgeConfigurationService;

    public BridgeConfigService(BridgeConfigurationService bridgeConfigurationService) {
        this.bridgeConfigurationService = bridgeConfigurationService;
    }

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