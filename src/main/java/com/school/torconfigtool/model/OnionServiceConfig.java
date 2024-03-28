package com.school.torconfigtool.model;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * This class represents the configuration for an Onion Service in the Tor network.
 */
@Data
@Component
public class OnionServiceConfig {

    private String OnionNickname;

    // The directory for the hidden service in the Tor network
    private String hiddenServiceDir;

    // The port for the hidden service in the Tor network
    private String hiddenServicePort;

    // The hostname of the Onion Service
    private String hostname;
}