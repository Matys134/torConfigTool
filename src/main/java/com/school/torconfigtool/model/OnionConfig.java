package com.school.torconfigtool.model;

import lombok.Data;

/**
 * OnionConfig class represents the configuration for an Onion service in the Tor network.
 */
@Data
public class OnionConfig {
    // The nickname of the Onion service
    private String nickname;
    // The hostname of the Onion service
    private String hostname;
    // The directory for the hidden service in the Tor network
    private String hiddenServiceDir;
    // The port for the hidden service in the Tor network
    private String hiddenServicePort;
}