package com.school.torconfigtool.service;

import com.school.torconfigtool.models.BaseRelayConfig;

public interface RelayConfigService<T extends BaseRelayConfig> {
    boolean updateConfiguration(T config);
}
