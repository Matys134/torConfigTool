package com.school.torconfigtool.bridge;

import com.school.torconfigtool.BridgeRelayConfig;
import com.school.torconfigtool.RelayConfigService;
import com.school.torconfigtool.TorrcFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service class for managing bridge relay configurations.
 * Implements the RelayConfigService interface for BridgeRelayConfig objects.
 */
@Service
public class BridgeConfigurationService implements RelayConfigService<BridgeRelayConfig> {

    // Logger for logging information and errors
    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigurationService.class);

    /**
     * Default constructor for BridgeConfigurationService.
     */
    public BridgeConfigurationService() {
    }

    /**
     * Updates the configuration for a bridge relay.
     * @param config The new configuration for the bridge relay.
     * @return true if the configuration was successfully updated, false otherwise.
     */
    @Override
    public boolean updateConfiguration(BridgeRelayConfig config) {
        try {
            // Build the file path for the torrc file
            String torrcFilePath = buildTorrcFilePath(config.getNickname());
            // Create the torrc file with the new configuration
            TorrcFileCreator.createTorrcFile(torrcFilePath, config);
            return true;
        } catch (Exception e) {
            // Log any errors that occur during the update
            logger.error("Error updating bridge relay configuration", e);
            return false;
        }
    }

    /**
     * Builds the file path for the torrc file.
     * @param nickname The nickname of the bridge relay.
     * @return The file path for the torrc file.
     */
    private String buildTorrcFilePath(String nickname) {
        // Use Path for file manipulation
        return String.format("torrc/torrc-%s_bridge", nickname);
    }
}