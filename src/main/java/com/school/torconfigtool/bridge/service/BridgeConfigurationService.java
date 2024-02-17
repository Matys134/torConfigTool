package com.school.torconfigtool.bridge.service;

import com.school.torconfigtool.bridge.config.BridgeRelayConfig;
import com.school.torconfigtool.RelayConfigService;
import com.school.torconfigtool.TorrcFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This service class is responsible for handling the configuration of a bridge relay.
 * It implements the RelayConfigService interface for BridgeRelayConfig type.
 */
@Service
public class BridgeConfigurationService implements RelayConfigService<BridgeRelayConfig> {

    // Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigurationService.class);

    /**
     * Default constructor for BridgeConfigurationService.
     */
    public BridgeConfigurationService() {
    }

    /**
     * This method updates the configuration for a bridge relay.
     * It creates a new torrc file with the given configuration.
     *
     * @param config The configuration for the bridge relay.
     * @return true if the configuration update was successful, false otherwise.
     */
    @Override
    public boolean updateConfiguration(BridgeRelayConfig config) {
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
     * This method builds the file path for the torrc file.
     * The file path is based on the nickname of the bridge relay.
     *
     * @param nickname The nickname of the bridge relay.
     * @return The file path for the torrc file.
     */
    private String buildTorrcFilePath(String nickname) {
        // Use Path for file manipulation
        return String.format("torrc/torrc-%s_bridge", nickname);
    }
}