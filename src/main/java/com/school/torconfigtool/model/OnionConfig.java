package com.school.torconfigtool.model;

import lombok.Data;

/**
 * This class represents the configuration for an Onion service in the Tor network.
 */
@Data
public class OnionConfig {
    // The directory for the hidden service in the Tor network
    private String hiddenServiceDir;

    // The port for the hidden service in the Tor network
    private String hiddenServicePort;

    // The hostname of the Tor network
    private String hostname;
}