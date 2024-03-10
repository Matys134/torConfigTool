package com.school.torconfigtool.model;

import lombok.Data;

/**
 * This is an abstract class that implements the RelayConfig interface.
 * It provides a base configuration for a relay in the Tor network.
 */
@Data
public abstract class BaseRelayConfig implements RelayConfig {
    // The nickname of the relay
    private String nickname;
    // The OR (Onion Router) port of the relay
    private String orPort;
    // The contact information for the relay
    private String contact;
    // The control port of the relay
    private String controlPort;
    // The bandwidth rate of the relay
    private String bandwidthRate;

}