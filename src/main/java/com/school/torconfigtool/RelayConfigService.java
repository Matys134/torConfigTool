package com.school.torconfigtool;

import com.school.torconfigtool.model.BaseRelayConfig;
import java.util.Map;

public interface RelayConfigService<T extends BaseRelayConfig> {

    boolean updateConfiguration(T config);

    String buildTorrcFilePath(String nickname);

    Map<String, String> updateConfigAndReturnResponse(T config);
}