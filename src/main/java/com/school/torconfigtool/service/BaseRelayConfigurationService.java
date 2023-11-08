package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.RelayController;
import com.school.torconfigtool.models.BaseRelayConfig;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseRelayConfigurationService<T extends BaseRelayConfig> {

    protected final RelayController relayController;

    @Autowired
    public BaseRelayConfigurationService(RelayController relayController) {
        this.relayController = relayController;
    }

    public abstract boolean updateConfiguration(T config);
}
