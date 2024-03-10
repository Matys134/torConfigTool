package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BaseRelayConfig;
import java.util.Map;

/**
 * This interface defines the contract for services that manage Relay Configurations.
 * The generic type T extends BaseRelayConfig, which means it can be any type that is a subclass of BaseRelayConfig.
 */
public interface RelayConfigService<T extends BaseRelayConfig> {

    /**
     * Updates the configuration with the provided config object.
     * @param config The configuration object to be updated.
     * @return A boolean indicating the success of the operation.
     */
    boolean updateConfiguration(T config);

    /**
     * Builds the file path for the Torrc file based on the provided nickname.
     * @param nickname The nickname to be used in the file path.
     * @return A string representing the file path.
     */
    String buildTorrcFilePath(String nickname);

    /**
     * Updates the configuration with the provided config object and returns a response.
     * @param config The configuration object to be updated.
     * @return A map containing the response details.
     */
    Map<String, String> updateConfigAndReturnResponse(T config);
}