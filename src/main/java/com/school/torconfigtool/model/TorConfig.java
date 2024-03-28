package com.school.torconfigtool.model;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * This class represents the configuration for a Tor network.
 * It includes configurations for guard nodes, bridge nodes, and onion services.
 */
@Data
@Component
public class TorConfig {

    // Configuration for the guard node in the Tor network.
    private GuardConfig guardConfig;

    // Configuration for the bridge node in the Tor network.
    private BridgeConfig bridgeConfig;

    // Configuration for the onion service in the Tor network.
    private OnionConfig onionConfig;
}