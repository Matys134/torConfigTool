package com.school.torconfigtool.service;

import com.school.torconfigtool.TorrcFileCreator;
import com.school.torconfigtool.model.BridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class for managing bridge configurations.
 */
@Service
public class BridgeConfigService {

    // Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigService.class);

    /**
     * Default constructor.
     */
    public BridgeConfigService() {
    }

    /**
     * Updates the bridge configuration with the provided config.
     * Logs the result of the operation and returns a response message.
     *
     * @param config The new bridge configuration.
     * @return A map containing a response message.
     */
    public Map<String, String> updateBridgeConfig(BridgeConfig config) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = updateConfiguration(config);
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

    /**
     * Attempts to update the bridge configuration.
     * Logs any exceptions that occur during the operation.
     *
     * @param config The new bridge configuration.
     * @return True if the operation was successful, false otherwise.
     */
    private boolean updateConfiguration(BridgeConfig config) {
        try {
            String torrcFilePath = buildTorrcFilePath(config.getNickname());
            TorrcFileCreator.createTorrcFile(torrcFilePath, config);
            return true;
        } catch (Exception e) {
            logger.error("Error updating bridge relay configuration", e);
            return false;
        }
    }

    /**
     * Builds the file path for the torrc file associated with the given nickname.
     *
     * @param nickname The nickname of the relay.
     * @return The file path for the torrc file.
     */
    private String buildTorrcFilePath(String nickname) {
        return String.format("torrc/torrc-%s_bridge", nickname);
    }
}