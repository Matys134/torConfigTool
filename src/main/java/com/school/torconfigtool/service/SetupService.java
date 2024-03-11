package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SetupService {

    private final RelayInformationService relayInformationService;

    public SetupService(RelayInformationService relayInformationService) {
        this.relayInformationService = relayInformationService;
    }

    /**
     * Gets the limit state and relay count.
     *
     * @return A map containing the limit state and count.
     */
    public Map<String, Object> getLimitStateAndCount() {
        Map<String, Object> response = new HashMap<>();
        response.put("limitOn", RelayInformationService.isLimitOn());
        response.put("guardCount", relayInformationService.getGuardCount());
        return response;
    }
}
