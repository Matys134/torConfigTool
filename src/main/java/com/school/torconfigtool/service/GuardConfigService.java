package com.school.torconfigtool.service;

import com.school.torconfigtool.model.TorrcFileCreator;
import com.school.torconfigtool.model.GuardConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GuardConfigService implements RelayConfigService<GuardConfig> {

    private static final Logger logger = LoggerFactory.getLogger(GuardConfigService.class);

    public GuardConfigService() {
    }

    @Override
    public boolean updateConfiguration(GuardConfig config) {
        try {
            String torrcFilePath = buildTorrcFilePath(config.getNickname());
            TorrcFileCreator.createTorrcFile(torrcFilePath, config);
            return true;
        } catch (Exception e) {
            logger.error("Error updating guard relay configuration", e);
            return false;
        }
    }

    @Override
    public String buildTorrcFilePath(String nickname) {
        return String.format("torrc/torrc-%s_guard", nickname);
    }

    @Override
    public Map<String, String> updateConfigAndReturnResponse(GuardConfig config) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = updateConfiguration(config);
            if (success) {
                logger.info("Guard configuration updated successfully for relay: {}", config.getNickname());
                response.put("message", "Guard configuration updated successfully");
            } else {
                logger.warn("Failed to update guard configuration for relay: {}", config.getNickname());
                response.put("message", "Failed to update guard configuration");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while updating guard configuration", e);
            response.put("message", "An unexpected error occurred");
        }
        return response;
    }
}