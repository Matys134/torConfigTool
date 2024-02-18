package com.school.torconfigtool;

import com.school.torconfigtool.model.BridgeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        Map<String, String> response = bridgeConfigService.updateBridgeConfig(config);
        if (response.get("message").equals("Bridge configuration updated successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}