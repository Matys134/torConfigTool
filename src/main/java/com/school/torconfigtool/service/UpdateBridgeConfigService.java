package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * Service class for updating the bridge configuration.
 */
@Service
public class UpdateBridgeConfigService implements RelayConfigService<BridgeConfig> {

    /**
     * Updates the configuration for a given BridgeConfig object.
     *
     * @param config The BridgeConfig object containing the configuration to be updated.
     * @return true if the configuration was successfully updated, false otherwise.
     */
    @Override
    public boolean updateConfiguration(BridgeConfig config) {
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
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Builds the file path for the torrc file based on the given nickname.
     *
     * @param nickname The nickname of the bridge.
     * @return The file path for the torrc file.
     */
    @Override
    public String buildTorrcFilePath(String nickname) {
        return String.format(TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + "%s_bridge", nickname);
    }

    /**
     * Updates the configuration for a given BridgeConfig object and returns a response.
     *
     * @param config The BridgeConfig object containing the configuration to be updated.
     * @return A map containing the status and message of the update operation.
     */
    @Override
    public Map<String, String> updateConfigAndReturnResponse(BridgeConfig config) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = updateConfiguration(config);
            if (success) {
                response.put("status", "success");
                response.put("message", "Bridge configuration updated successfully for relay: " + config.getNickname());
            } else {
                response.put("status", "failure");
                response.put("message", "Failed to update bridge configuration.");
            }
        } catch (Exception e) {
            response.put("status", "failure");
            response.put("message", "An unexpected error occurred while updating the configuration.");
        }
        return response;
    }
}