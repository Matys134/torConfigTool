package com.school.torconfigtool.service;

import com.school.torconfigtool.model.GuardConfig;
import org.springframework.stereotype.Service;

import static com.school.torconfigtool.util.Constants.TORRC_DIRECTORY_PATH;
import static com.school.torconfigtool.util.Constants.TORRC_FILE_PREFIX;

@Service
public class UpdateGuardConfigService extends AbstractUpdateConfigService<GuardConfig> {

    @Override
    public String buildTorrcFilePath(String nickname) {
        return String.format(TORRC_DIRECTORY_PATH + TORRC_FILE_PREFIX + "%s_guard", nickname);
    }

    @Override
    protected String getSuccessMessage(GuardConfig config) {
        return "Guard configuration updated successfully for relay: " + config.getNickname();
    }
}