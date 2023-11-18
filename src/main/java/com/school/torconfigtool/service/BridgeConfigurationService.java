package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.RelayController;
import com.school.torconfigtool.models.BridgeRelayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BridgeConfigurationService implements RelayConfigService<BridgeRelayConfig> {

    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigurationService.class);

    private final RelayController relayController;

    public BridgeConfigurationService(RelayController relayController) {
        this.relayController = relayController;
    }

    @Override
    public boolean updateConfiguration(BridgeRelayConfig config) {
        try {
            String torrcFilePath = buildTorrcFilePath(config.getNickname());
            relayController.createTorrcFile(torrcFilePath, config);
            return true;
        } catch (Exception e) {
            logger.error("Error updating bridge relay configuration", e);
            return false;
        }
    }

    private String buildTorrcFilePath(String nickname) {
        // Use Path for file manipulation
        return String.format("torrc/bridge/local-torrc-%s", nickname);
    }
}
