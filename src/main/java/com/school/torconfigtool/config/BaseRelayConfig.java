package com.school.torconfigtool.config;

import com.school.torconfigtool.relay.RelayConfig;
import lombok.Data;

import java.io.BufferedWriter;
import java.io.IOException;

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

    /**
     * This is an abstract method that must be implemented by any class that extends BaseRelayConfig.
     * It is used to write the specific configuration of the relay.
     *
     * @param writer a BufferedWriter object that is used to write the configuration to a file
     * @throws IOException if an I/O error occurs
     */
    @Override
    public abstract void writeSpecificConfig(BufferedWriter writer) throws IOException;
}