package com.school.torconfigtool.service;

import com.school.torconfigtool.RelayConfigService;
import com.school.torconfigtool.TorrcFileCreator;
import com.school.torconfigtool.model.GuardConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * This service class is responsible for managing the configuration of Guard Relays in the Tor network.
 * It implements the RelayConfigService interface for GuardRelayConfig objects.
 */
@Service
public class GuardConfigService implements RelayConfigService<GuardConfig> {

    // Logger instance for logging events of this class
    private static final Logger logger = LoggerFactory.getLogger(GuardConfigService.class);

    /**
     * Default constructor for GuardConfigurationService.
     */
    public GuardConfigService() {
    }

    /**
     * Updates the configuration of a Guard Relay.
     * It creates a new torrc file with the given configuration.
     *
     * @param config The new configuration for the Guard Relay.
     * @return true if the configuration was updated successfully, false otherwise.
     */
    @Override
    public boolean updateConfiguration(GuardConfig config) {
        try {
            String torrcFilePath = buildTorrcFilePath(config.getNickname());
            TorrcFileCreator.createTorrcFile(torrcFilePath, config);
            return true;
        } catch (Exception e) {
            logger.error("Error updating guard relay configuration", e);
            return false;
        }
    }

    /**
     * Builds the file path for the torrc file of a Guard Relay.
     * The file path is built based on the nickname of the Guard Relay.
     *
     * @param nickname The nickname of the Guard Relay.
     * @return The file path for the torrc file.
     */
    private String buildTorrcFilePath(String nickname) {
        // Use Path for file manipulation
        return String.format("torrc/torrc-%s_guard", nickname);
    }

    /**
     * Updates the Guard Relay configuration and returns a ResponseEntity with the result of the operation.
     * If the update is successful, it returns a 200 OK response with a success message.
     * If the update fails, it returns a 500 Internal Server Error response with an error message.
     *
     * @param config The new configuration for the Guard Relay.
     * @return a ResponseEntity with the result of the operation.
     */
    public ResponseEntity<?> updateGuardConfiguration(GuardConfig config) {
        try {
            boolean success = updateConfiguration(config);
            if (success) {
                logger.info("Guard configuration updated successfully for relay: {}", config.getNickname());
                return ResponseEntity.ok(Map.of("success", "Guard configuration updated successfully"));
            } else {
                logger.warn("Failed to update guard configuration for relay: {}", config.getNickname());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to update guard configuration"));
            }
        } catch (Exception e) {
            logger.error("Exception occurred while updating guard configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred"));
        }
    }
}