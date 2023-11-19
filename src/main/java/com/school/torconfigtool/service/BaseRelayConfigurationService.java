package com.school.torconfigtool.service;

import com.school.torconfigtool.controllers.GuardController;
import com.school.torconfigtool.models.BaseRelayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class BaseRelayConfigurationService<T extends BaseRelayConfig> {

    protected final GuardController guardController;

    @Autowired
    public BaseRelayConfigurationService(GuardController guardController) {
        this.guardController = guardController;
    }

    public abstract boolean updateConfiguration(T config);
}
