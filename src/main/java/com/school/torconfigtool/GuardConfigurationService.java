package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GuardConfigurationService implements RelayConfigService<GuardRelayConfig> {

    private static final Logger logger = LoggerFactory.getLogger(GuardConfigurationService.class);

    public GuardConfigurationService() {
    }

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

    private String buildTorrcFilePath(String nickname) {
        // Use Path for file manipulation
        return String.format("torrc/torrc-%s_guard", nickname);
    }
}
