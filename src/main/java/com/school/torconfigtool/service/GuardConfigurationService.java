package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.RelayController;
import com.school.torconfigtool.models.GuardRelayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GuardConfigurationService implements RelayConfigService<GuardRelayConfig> {

    private static final Logger logger = LoggerFactory.getLogger(GuardConfigurationService.class);

    private final RelayController relayController;

    public GuardConfigurationService(RelayController relayController) {
        this.relayController = relayController;
    }

    @Override
    public boolean updateConfiguration(GuardRelayConfig config) {
        try {
            String torrcFilePath = buildTorrcFilePath(config.getNickname());
            relayController.createTorrcFile(torrcFilePath, config);
            return true;
        } catch (Exception e) {
            logger.error("Error updating guard relay configuration", e);
            return false;
        }
    }

    private String buildTorrcFilePath(String nickname) {
        // Use Path for file manipulation
        return String.format("torrc/guard/local-torrc-%s", nickname);
    }
}
