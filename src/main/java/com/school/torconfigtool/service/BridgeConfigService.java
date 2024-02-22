package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.model.TorrcFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
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
            String filePath = buildTorrcFilePath(config.getNickname());
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            TorrcFileCreator.createTorrcFile(filePath, config);
            return true;
        } catch (Exception e) {
            logger.error("Failed to update bridge configuration for relay: " + config.getNickname(), e);
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
        try {
            boolean success = updateConfiguration(config);
            if (success) {
                logger.info("Bridge configuration updated successfully for relay: {}", config.getNickname());
                response.put("status", "success");
                response.put("message", "Bridge configuration updated successfully for relay: " + config.getNickname());
            } else {
                logger.warn("Failed to update bridge configuration for relay: {}", config.getNickname());
                response.put("status", "failure");
                response.put("message", "Failed to update bridge configuration.");
            }
        } catch (Exception e) {
            logger.error("Error updating Bridge configuration for relay: " + config.getNickname(), e);
            response.put("status", "failure");
            response.put("message", "An unexpected error occurred while updating the configuration.");
        }
        return response;
    }
}