package com.school.torconfigtool.service;

import com.school.torconfigtool.model.GuardConfig;
import org.springframework.stereotype.Service;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

/**
 * Service class for updating the guard configuration.
 * This class extends the AbstractUpdateConfigService with GuardConfig as the type parameter.
 */
@Service
public class UpdateGuardConfigService extends AbstractUpdateConfigService<GuardConfig> {

    /**
     * Builds the file path for the torrc file based on the nickname of the guard.
     *
     * @param nickname The nickname of the guard.
     * @return The file path of the torrc file.
     */
    @Override
    public String buildTorrcFilePath(String nickname) {
        return String.format(TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + "%s_guard", nickname);
    }

    /**
     * Returns a success message after the guard configuration is updated.
     *
     * @param config The updated guard configuration.
     * @return The success message.
     */
    @Override
    protected String getSuccessMessage(GuardConfig config) {
        return "Guard configuration updated successfully for relay: " + config.getNickname();
    }
}