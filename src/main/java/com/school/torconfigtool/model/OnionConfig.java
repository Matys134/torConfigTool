package com.school.torconfigtool.model;

import lombok.Data;

/**
 * This class represents the configuration for an Onion service in the Tor network.
 */
@Data
public class OnionConfig implements RelayConfig {

    // The directory for the hidden service in the Tor network
    private String hiddenServiceDir;

    // The port for the hidden service in the Tor network
    private String hiddenServicePort;

    // The bandwidth rate of the Tor network
    private String bandwidthRate;

    // The hostname of the Tor network
    private String hostname;

    // The nickname of the Tor network
    private String nickname;

    @Override
    public String getOrPort() {
        return null;
    }

    @Override
    public void setOrPort(String orPort) {

    }

    @Override
    public String getContact() {
        return null;
    }

    @Override
    public void setContact(String contact) {

    }

    @Override
    public String getControlPort() {
        return null;
    }

    @Override
    public void setControlPort(String controlPort) {

    }
}