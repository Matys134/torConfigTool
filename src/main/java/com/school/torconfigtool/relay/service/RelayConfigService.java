package com.school.torconfigtool.relay.service;

import com.school.torconfigtool.config.BaseRelayConfig;

/**
 * The RelayConfigService interface provides a method for updating the configuration of a relay.
 * The configuration object must extend the BaseRelayConfig class.
 *
 * @param <T> The type of the configuration object. It must extend BaseRelayConfig.
 */
public interface RelayConfigService<T extends BaseRelayConfig> {

    /**
     * Updates the configuration of a relay.
     *
     * @param config The new configuration object.
     * @return true if the configuration was successfully updated, false otherwise.
     */
    boolean updateConfiguration(T config);
}