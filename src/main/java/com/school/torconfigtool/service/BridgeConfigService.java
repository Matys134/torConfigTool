package com.school.torconfigtool.service;

import com.school.torconfigtool.model.TorrcFileCreator;
import com.school.torconfigtool.model.BridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.school.torconfigtool.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.Constants.TORRC_FILE_PREFIX;

@Service
public class BridgeConfigService implements RelayConfigService<BridgeConfig> {

    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigService.class);

    public BridgeConfigService() {
    }

    @Override
    public boolean updateConfiguration(BridgeConfig config) {
        try {
            String torrcFilePath = buildTorrcFilePath(config.getNickname());
            boolean success = TorrcFileCreator.createTorrcFile(torrcFilePath, config);
            if (!success) {
                logger.warn("Failed to create torrc file for relay: {}", config.getNickname());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error updating bridge relay configuration", e);
            return false;
        }
    }

    @Override
    public String buildTorrcFilePath(String nickname) {
        return String.format(TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + "%s_bridge", nickname);
    }

    @Override
    public Map<String, String> updateConfigAndReturnResponse(BridgeConfig config) {
        Map<String, String> response = new HashMap<>();
        boolean success = false;
        try {
            success = updateConfiguration(config);
            if (success) {
                logger.info("Bridge configuration updated successfully for relay: {}", config.getNickname());
                response.put("message", "Bridge configuration updated successfully");
            } else {
                logger.warn("Failed to update bridge configuration for relay: {}", config.getNickname());
                response.put("message", "Failed to update bridge configuration");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while updating bridge configuration", e);
            if (!success) {
                response.put("message", "An unexpected error occurred");
            }
        }
        return response;
    }
}