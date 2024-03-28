package com.school.torconfigtool.model;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * This class represents the configuration for a Tor network.
 * It includes configurations for guard nodes, bridge nodes, and hidden services.
 * It also includes the hostname and bandwidth rate for the Tor network.
 *
 */
@Data
@Component
public class TorConfig {

    // Configuration for the guard node in the Tor network.
    private GuardConfig guardConfig;

    // Configuration for the bridge node in the Tor network.
    private BridgeConfig bridgeConfig;

    // The directory for the hidden service in the Tor network
    private String hiddenServiceDir;

    // The port for the hidden service in the Tor network
    private String hiddenServicePort;

    // The bandwidth rate of the Tor network
    private String bandwidthRate;

    // The hostname of the Tor network
    private String hostname;
}