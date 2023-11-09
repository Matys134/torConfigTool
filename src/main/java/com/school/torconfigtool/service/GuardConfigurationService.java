package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.RelayController;
import com.school.torconfigtool.models.GuardRelayConfig;
import org.springframework.stereotype.Service;

@Service
public class GuardConfigurationService implements RelayConfigService<GuardRelayConfig> {

    private final RelayController relayController;

    public GuardConfigurationService(RelayController relayController) {
        this.relayController = relayController;
    }

    @Override
    public boolean updateConfiguration(GuardRelayConfig config) {
        try {
            relayController.createTorrcFile(
                    "torrc/guard/local-torrc-" + config.getNickname(),
                    config
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
