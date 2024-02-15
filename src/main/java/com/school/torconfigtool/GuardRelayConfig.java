package com.school.torconfigtool;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.BufferedWriter;

/**
 * This class extends the BaseRelayConfig class and represents a specific type of relay configuration - GuardRelayConfig.
 * It currently does not add any additional properties or behaviors to the base class.
 * However, it can be expanded in the future if specific properties or behaviors for GuardRelayConfig are needed.
 *
 * @author Matys134
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GuardRelayConfig extends BaseRelayConfig {

    /**
     * This method is an override of the abstract method in the superclass.
     * It is currently empty, but can be used to write specific configuration for a GuardRelayConfig in the future.
     *
     * @param writer a BufferedWriter object to write the configuration
     */
    @Override
    public void writeSpecificConfig(BufferedWriter writer) {
    }
}