package com.school.torconfigtool.service;

import com.school.torconfigtool.model.BridgeConfig;
import org.springframework.stereotype.Service;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * Service class for updating the bridge configuration.
 * This class extends the AbstractUpdateConfigService with BridgeConfig as the type parameter.
 */
@Service
public class UpdateBridgeConfigService extends AbstractUpdateConfigService<BridgeConfig> {

    /**
     * Builds the file path for the torrc file based on the nickname of the bridge.
     *
     * @param nickname The nickname of the bridge.
     * @return The file path of the torrc file.
     */
    @Override
    public String buildTorrcFilePath(String nickname) {
        return String.format(TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + "%s_bridge", nickname);
    }

    /**
     * Returns a success message after the bridge configuration is updated.
     *
     * @param config The updated bridge configuration.
     * @return The success message.
     */
    @Override
    protected String getSuccessMessage(BridgeConfig config) {
        return "Bridge configuration updated successfully for relay: " + config.getNickname();
    }
}