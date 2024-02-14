package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BridgeConfigurationService implements RelayConfigService<BridgeRelayConfig> {

    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigurationService.class);

    public BridgeConfigurationService() {
    }

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

    private String buildTorrcFilePath(String nickname) {
        // Use Path for file manipulation
        return String.format("torrc/torrc-%s_bridge", nickname);
    }
}
