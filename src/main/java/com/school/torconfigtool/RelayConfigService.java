package com.school.torconfigtool;

public interface RelayConfigService<T extends BaseRelayConfig> {
    boolean updateConfiguration(T config);
}
