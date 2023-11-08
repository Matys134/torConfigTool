package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.RelayController;
import com.school.torconfigtool.models.BridgeRelayConfig;
import org.springframework.stereotype.Service;

@Service
public class BridgeConfigurationService extends BaseRelayConfigurationService<BridgeRelayConfig> {

    public BridgeConfigurationService(RelayController relayController) {
        super(relayController);
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
