package com.school.torconfigtool.service;

import com.school.torconfigtool.model.GuardConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

@Service
public class GuardConfigService implements RelayConfigService<GuardConfig> {

    public GuardConfigService() {
    }

    @Override
    public boolean updateConfiguration(GuardConfig config) {
        try {
            String filePath = buildTorrcFilePath(config.getNickname());
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleteResult = file.delete();
                if (!deleteResult) {
                    // Handle the case where the file was not successfully deleted,
                    // For example, you could throw an IOException
                    throw new IOException("Failed to delete file: " + filePath);
                }
            }
            TorrcFileCreator.createTorrcFile(filePath, config);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String buildTorrcFilePath(String nickname) {
        return String.format(TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + "%s_guard", nickname);
    }

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