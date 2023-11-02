package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.RelayController;
import com.school.torconfigtool.models.BridgeRelayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BridgeConfigurationService {

    private final RelayController relayController;

    @Autowired
    public BridgeConfigurationService(RelayController relayController) {
        this.relayController = relayController;
    }

    public boolean updateBridgeConfiguration(BridgeRelayConfig config) {
        try {
            // Call the relevant method in RelayController using config attributes
            relayController.createTorrcFile("torrc/bridge/local-torrc-" + config.getNickname(), config.getNickname(), null, Integer.parseInt(config.getOrPort()), config.getContact(), Integer.parseInt(config.getControlPort()), Integer.parseInt(config.getSocksPort()));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
