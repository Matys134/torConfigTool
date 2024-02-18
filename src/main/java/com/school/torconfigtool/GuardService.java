package com.school.torconfigtool;

import com.school.torconfigtool.model.GuardConfig;
import org.springframework.stereotype.Service;

@Service
public class GuardService {

    /**
     * Creates a new GuardRelayConfig object with the given parameters.
     *
     * @param relayNickname   The nickname of the Guard Relay.
     * @param relayPort       The OR port of the Guard Relay.
     * @param relayContact    The contact information of the Guard Relay.
     * @param controlPort     The control port of the Guard Relay.
     * @param relayBandwidth  The bandwidth of the Guard Relay.
     * @return A new GuardRelayConfig object with the given parameters.
     */
    public GuardConfig createGuardConfig(String relayNickname, int relayPort, String relayContact, int controlPort, Integer relayBandwidth) {
        GuardConfig config = new GuardConfig();
        config.setNickname(relayNickname);
        config.setOrPort(String.valueOf(relayPort));
        config.setContact(relayContact);
        config.setControlPort(String.valueOf(controlPort));
        if (relayBandwidth != null) {
            config.setBandwidthRate(String.valueOf(relayBandwidth));
        }

        return config;
    }
}
