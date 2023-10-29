package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.RelayController;
import com.school.torconfigtool.models.GuardRelayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuardConfigurationService {
    private final RelayController relayController;

    @Autowired
    public GuardConfigurationService(RelayController relayController) {
        this.relayController = relayController;
    }

    public boolean updateGuardConfiguration(GuardRelayConfig config) {
        try {
            // Call the relevant method in RelayController using config attributes
            relayController.createTorrcFile("torrc/guard/local-torrc-" + config.getNickname(), config.getNickname(), null, Integer.parseInt(config.getOrPort()), config.getContact(), Integer.parseInt(config.getControlPort()), Integer.parseInt(config.getSocksPort()));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
