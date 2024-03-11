package com.school.torconfigtool.controller;

import com.school.torconfigtool.model.BridgeConfig;
import com.school.torconfigtool.service.BridgeConfigService;
import com.school.torconfigtool.service.RelayUtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/update-bridge-config")
public class BridgeConfigController {
    private final BridgeConfigService bridgeConfigService;

    @Autowired
    public BridgeConfigController(BridgeConfigService bridgeConfigService) {
        this.bridgeConfigService = bridgeConfigService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> updateBridgeConfiguration(@RequestBody BridgeConfig config) {
        Map<String, String> response = bridgeConfigService.updateConfigAndReturnResponse(config);
        if (response.get("message").startsWith("Bridge configuration updated successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check-port-availability")
    public ResponseEntity<?> checkPortAvailability(@RequestParam String nickname, @RequestParam int orPort, @RequestParam int controlPort) {
        boolean arePortsAvailable = RelayUtilityService.portsAreAvailable(nickname, orPort, controlPort);

        return ResponseEntity.ok(Map.of("available", arePortsAvailable));
    }
}