package com.school.torconfigtool;

import com.school.torconfigtool.config.BaseRelayConfig;

public interface RelayConfigService<T extends BaseRelayConfig> {
    boolean updateConfiguration(T config);
}
