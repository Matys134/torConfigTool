package com.school.torconfigtool.guard.service;

import com.school.torconfigtool.guard.config.GuardRelayConfig;
import com.school.torconfigtool.relay.service.RelayConfigService;
import com.school.torconfigtool.TorrcFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This service class is responsible for handling the configuration of the Guard Relay.
 * It implements the RelayConfigService interface with GuardRelayConfig as the type parameter.
 */
@Service
public class GuardConfigurationService implements RelayConfigService<GuardRelayConfig> {

    // Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(GuardConfigurationService.class);

    /**
     * Default constructor for GuardConfigurationService.
     */
    public GuardConfigurationService() {
    }

    /**
     * This method updates the configuration of the Guard Relay.
     * It creates a new torrc file with the given configuration.
     * If the operation is successful, it returns true. If an exception occurs, it logs the error and returns false.
     *
     * @param config The new configuration for the Guard Relay.
     * @return true if the operation is successful, false otherwise.
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
     * This method builds the file path for the torrc file based on the nickname of the Guard Relay.
     *
     * @param nickname The nickname of the Guard Relay.
     * @return The file path for the torrc file.
     */
    private String buildTorrcFilePath(String nickname) {
        // Use Path for file manipulation
        return String.format("torrc/torrc-%s_guard", nickname);
    }
}