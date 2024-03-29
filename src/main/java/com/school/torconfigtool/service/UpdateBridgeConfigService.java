package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import org.springframework.stereotype.Service;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

@Service
public class UpdateBridgeConfigService extends AbstractUpdateConfigService<BridgeConfig> {

    @Override
    public String buildTorrcFilePath(String nickname) {
        return String.format(TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + "%s_bridge", nickname);
    }

    @Override
    protected String getSuccessMessage(BridgeConfig config) {
        return "Bridge configuration updated successfully for relay: " + config.getNickname();
    }
}