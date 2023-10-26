package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.RelayController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GuardConfigurationService {
    private final RelayController relayController;

    @Autowired
    public GuardConfigurationService(RelayController relayController) {
        this.relayController = relayController;
    }

    public boolean updateGuardConfiguration(String nickname, String orPort, String contact) {
        try {
            // Call the relevant method in RelayController
            relayController.createTorrcFile("torrc/guard/local-torrc-" + nickname, nickname, null, Integer.parseInt(orPort), contact);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
