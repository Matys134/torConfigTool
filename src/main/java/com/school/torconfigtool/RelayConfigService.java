package com.school.torconfigtool;

import com.school.torconfigtool.model.BaseRelayConfig;

/**
 * This interface defines the methods for managing the configuration of a Tor relay.
 * It is a generic interface that can be implemented with any class that extends BaseRelayConfig.
 *
 * @param <T> The type of the relay configuration. It must extend BaseRelayConfig.
 */
public interface RelayConfigService<T extends BaseRelayConfig> {

    /**
     * Updates the configuration of the Tor relay with the specified configuration.
     * If the update is successful, it returns true.
     * Otherwise, it returns false.
     *
     * @param config The configuration to be used for the update.
     * @return True if the update is successful, or false otherwise.
     */
    boolean updateConfiguration(T config);
}