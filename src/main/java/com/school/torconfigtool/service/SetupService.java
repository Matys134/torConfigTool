package com.school.torconfigtool.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SetupService {

    /**
     * Gets the limit state
     *
     * @return A map containing the limit state
     */
    public Map<String, Object> getLimitState() {
        Map<String, Object> response = new HashMap<>();
        response.put("limitOn", RelayInformationService.isLimitOn());
        return response;
    }
}
