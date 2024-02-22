package com.school.torconfigtool.service;

import com.school.torconfigtool.model.TorrcFileCreator;
import com.school.torconfigtool.model.GuardConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.school.torconfigtool.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.Constants.TORRC_FILE_PREFIX;

@Service
public class GuardConfigService implements RelayConfigService<GuardConfig> {

    private static final Logger logger = LoggerFactory.getLogger(GuardConfigService.class);

    public GuardConfigService() {
    }

    @Override
    public boolean updateConfiguration(GuardConfig config) {
        try {
            String torrcFilePath = buildTorrcFilePath(config.getNickname());
            boolean success = TorrcFileCreator.createTorrcFile(torrcFilePath, config);
            if (!success) {
                logger.warn("Failed to create torrc file for relay: {}", config.getNickname());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error updating guard relay configuration", e);
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
        boolean success = false;
        try {
            success = updateConfiguration(config);
            if (success) {
                logger.info("Guard configuration updated successfully for relay: {}", config.getNickname());
                response.put("message", "Guard configuration updated successfully");
            } else {
                logger.warn("Failed to update guard configuration for relay: {}", config.getNickname());
                response.put("message", "Failed to update guard configuration");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while updating guard configuration", e);
            if (!success) {
                response.put("message", "An unexpected error occurred");
            }
        }
        return response;
    }
}