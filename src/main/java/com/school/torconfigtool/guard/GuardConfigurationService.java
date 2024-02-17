package com.school.torconfigtool.guard;

import com.school.torconfigtool.GuardRelayConfig;
import com.school.torconfigtool.RelayConfigService;
import com.school.torconfigtool.TorrcFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This service class is responsible for managing the configuration of Guard Relays in the Tor network.
 * It implements the RelayConfigService interface for GuardRelayConfig objects.
 */
@Service
public class GuardConfigurationService implements RelayConfigService<GuardRelayConfig> {

    // Logger instance for logging events of this class
    private static final Logger logger = LoggerFactory.getLogger(GuardConfigurationService.class);

    /**
     * Default constructor for GuardConfigurationService.
     */
    public GuardConfigurationService() {
    }

    /**
     * Updates the configuration of a Guard Relay.
     * It creates a new torrc file with the given configuration.
     *
     * @param config The new configuration for the Guard Relay.
     * @return true if the configuration was updated successfully, false otherwise.
     */
    @Override
    public boolean updateConfiguration(GuardRelayConfig config) {
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
}