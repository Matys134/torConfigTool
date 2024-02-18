package com.school.torconfigtool;

import com.school.torconfigtool.BaseRelayConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.BufferedWriter;

/**
 * GuardConfig class extends the BaseRelayConfig class.
 * It represents the configuration for a Guard Relay in the Tor network.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GuardConfig extends BaseRelayConfig {

    /**
     * Writes the specific configuration for a Guard Relay.
     * This method is meant to be overridden in subclasses to provide specific configuration.
     *
     * @param writer the BufferedWriter to write the configuration to
     */
    @Override
    public void writeSpecificConfig(BufferedWriter writer) {
    }
}