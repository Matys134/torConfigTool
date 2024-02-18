package com.school.torconfigtool.controller;

import com.school.torconfigtool.GuardConfig;
import com.school.torconfigtool.GuardConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/update-guard-config")
public class GuardConfigController {
    private final GuardConfigService guardConfigService;

    public GuardConfigController(GuardConfigService guardConfigService) {
        this.guardConfigService = guardConfigService;
    }

    @PostMapping
    public ResponseEntity<?> updateGuardConfiguration(@RequestBody GuardConfig config) {
        return guardConfigService.updateGuardConfiguration(config);
    }
}
