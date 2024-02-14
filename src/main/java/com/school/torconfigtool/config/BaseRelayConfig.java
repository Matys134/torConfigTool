package com.school.torconfigtool.config;

import com.school.torconfigtool.RelayConfig;
import lombok.Data;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * This is an abstract class that implements the RelayConfig interface.
 * It provides a base configuration for a relay with common properties like nickname, orPort, contact, controlPort, and bandwidthRate.
 * It also declares an abstract method for writing specific configuration which needs to be implemented by subclasses.
 */
@Data
public abstract class BaseRelayConfig implements RelayConfig {
    // The nickname of the relay
    private String nickname;
    // The OR (Onion Router) port of the relay
    private String orPort;
    // The contact information of the relay
    private String contact;
    // The control port of the relay
    private String controlPort;
    // The bandwidth rate of the relay
    private String bandwidthRate;

    /**
     * This is an abstract method that needs to be implemented by subclasses.
     * It is used to write specific configuration for a relay.
     *
     * @param writer a BufferedWriter object to write the configuration
     * @throws IOException if an I/O error occurs
     */
    @Override
    public abstract void writeSpecificConfig(BufferedWriter writer) throws IOException;
}