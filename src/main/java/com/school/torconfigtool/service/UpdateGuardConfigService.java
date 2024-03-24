package com.school.torconfigtool.service;

import com.school.torconfigtool.model.GuardConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * Service class for updating the Guard Configuration.
 * Implements the RelayConfigService interface.
 */
@Service
public class UpdateGuardConfigService implements RelayConfigService<GuardConfig> {

    /**
     * Updates the configuration for a given GuardConfig.
     * If a file with the same name exists, it is deleted and a new one is created.
     * @param config The GuardConfig to update.
     * @return true if the update was successful, false otherwise.
     */
    @Override
    public boolean updateConfiguration(GuardConfig config) {
        try {
            String filePath = buildTorrcFilePath(config.getNickname());
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleteResult = file.delete();
                if (!deleteResult) {
                    throw new IOException("Failed to delete file: " + filePath);
                }
            }
            TorrcFileCreator.createTorrcFile(filePath, config);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Builds the file path for the Torrc file.
     * @param nickname The nickname of the GuardConfig.
     * @return The file path as a string.
     */
    @Override
    public String buildTorrcFilePath(String nickname) {
        return String.format(TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + "%s_guard", nickname);
    }

    /**
     * Updates the configuration and returns a response.
     * The response contains the status and a message.
     * @param config The GuardConfig to update.
     * @return A map containing the status and a message.
     */
    @Override
    public Map<String, String> updateConfigAndReturnResponse(GuardConfig config) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = updateConfiguration(config);
            if (success) {
                response.put("status", "success");
                response.put("message", "Guard configuration updated successfully for relay: " + config.getNickname());
            } else {
                response.put("status", "failure");
                response.put("message", "Failed to update guard configuration.");
            }
        } catch (Exception e) {
            response.put("status", "failure");
            response.put("message", "An unexpected error occurred while updating the configuration.");
        }
        return response;
    }
}