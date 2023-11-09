package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.RelayController;
import com.school.torconfigtool.models.BridgeRelayConfig;
import org.springframework.stereotype.Service;

@Service
public class BridgeConfigurationService implements RelayConfigService<BridgeRelayConfig> {

    private final RelayController relayController;

    public BridgeConfigurationService(RelayController relayController) {
        this.relayController = relayController;
    }

    @Override
    public boolean updateConfiguration(BridgeRelayConfig config) {
        try {
            relayController.createTorrcFile(
                    "torrc/bridge/local-torrc-" + config.getNickname(),
                    config
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
