package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BaseRelayConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AbstractUpdateConfigService is an abstract class that provides a template for updating configurations.
 * It implements the UpdateConfigService interface.
 * @param <T> The type of configuration to be updated. It must extend BaseRelayConfig.
 */
public abstract class AbstractUpdateConfigService<T extends BaseRelayConfig> implements UpdateConfigService<T> {

    /**
     * Updates the configuration.
     * @param config The configuration to be updated.
     * @return A map containing the status and message of the operation.
     */
    @Override
    public Map<String, String> updateConfiguration(T config) {
        Map<String, String> response = new HashMap<>();
        try {
            String filePath = buildTorrcFilePath(config.getNickname());
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleteResult = file.delete();
                if (!deleteResult) {
                    throw new IOException("Failed to delete existing file: " + filePath);
                }
            }
            TorrcFileCreator.createTorrcFile(filePath, config);
            response.put("status", "success");
            response.put("message", getSuccessMessage(config));
        } catch (Exception e) {
            response.put("status", "failure");
            response.put("message", "An unexpected error occurred while updating the configuration.");
        }
        return response;
    }

    /**
     * Returns the success message for the operation.
     * This method is abstract and must be implemented by subclasses.
     * @param config The configuration that was updated.
     * @return The success message.
     */
    protected abstract String getSuccessMessage(T config);
}