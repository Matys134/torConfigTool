package com.school.torconfigtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/update-guard-config")
public class GuardConfigurationController {
    private final GuardConfigurationService guardConfigurationService;

    @Autowired
    public GuardConfigurationController(GuardConfigurationService guardConfigurationService) {
        this.guardConfigurationService = guardConfigurationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> updateGuardConfiguration(
            @RequestParam String nickname,
            @RequestParam String orPort,
            @RequestParam String contact) {
        Map<String, String> response = new HashMap<>();

        boolean success = guardConfigurationService.updateGuardConfiguration(nickname, orPort, contact);

        if (success) {
            response.put("success", "true");
        } else {
            response.put("success", "false");
        }

        return ResponseEntity.ok(response);
    }
}
