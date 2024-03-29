package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BaseRelayConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractUpdateConfigService<T extends BaseRelayConfig> implements UpdateConfigService<T> {

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

    protected abstract String getSuccessMessage(T config);
}