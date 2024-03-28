package com.school.torconfigtool.model;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * This class represents the configuration for an Onion service in the Tor network.
 * It includes the directory, port, bandwidth rate, and hostname for the service.
 *
 */
@Data
@Component
public class OnionConfig implements RelayConfig {

    // The directory for the hidden service in the Tor network
    private String hiddenServiceDir;

    // The port for the hidden service in the Tor network
    private String hiddenServicePort;

    // The bandwidth rate of the Tor network
    private String bandwidthRate;

    // The hostname of the Tor network
    private String hostname;

    @Override
    public String getNickname() {
        return null;
    }

    @Override
    public void setNickname(String nickname) {

    }

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