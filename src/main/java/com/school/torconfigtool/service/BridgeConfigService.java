package com.school.torconfigtool.service;

import com.school.torconfigtool.model.TorrcFileCreator;
import com.school.torconfigtool.model.BridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BridgeConfigService implements RelayConfigService<BridgeConfig> {

    private static final Logger logger = LoggerFactory.getLogger(BridgeConfigService.class);

    public BridgeConfigService() {
    }

    @Override
    public boolean updateConfiguration(BridgeConfig config) {
        try {
            String torrcFilePath = buildTorrcFilePath(config.getNickname());
            TorrcFileCreator.createTorrcFile(torrcFilePath, config);
            return true;
        } catch (Exception e) {
            logger.error("Error updating bridge relay configuration", e);
            return false;
        }
    }

    @Override
    public String buildTorrcFilePath(String nickname) {
        return String.format("torrc/torrc-%s_bridge", nickname);
    }

    @Override
    public Map<String, String> updateConfigAndReturnResponse(BridgeConfig config) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = updateConfiguration(config);
            if (success) {
                logger.info("Bridge configuration updated successfully for relay: {}", config.getNickname());
                response.put("message", "Bridge configuration updated successfully");
            } else {
                logger.warn("Failed to update bridge configuration for relay: {}", config.getNickname());
                response.put("message", "Failed to update bridge configuration");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while updating bridge configuration", e);
            response.put("message", "An unexpected error occurred");
        }
        return response;
    }
}