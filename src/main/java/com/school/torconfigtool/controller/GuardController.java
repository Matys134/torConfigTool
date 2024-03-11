package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.GuardService;
import com.school.torconfigtool.service.RelayUtilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/guard")
public class GuardController {

    private final GuardService guardService;

    public GuardController(GuardService guardService) {
        this.guardService = guardService;
    }

    @GetMapping
    public String guardConfigurationForm() {
        return "setup";
    }

    @PostMapping("/configure")
    public String configureGuard(@RequestParam String relayNickname,
                                 @RequestParam int relayPort,
                                 @RequestParam String relayContact,
                                 @RequestParam int controlPort,
                                 @RequestParam(required = false) Integer guardBandwidth,
                                 Model model) {
        try {
            guardService.configureGuard(relayNickname, relayPort, relayContact, controlPort, guardBandwidth);
            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to configure Tor Relay: " + e.getMessage());
        }
        return "setup";
    }

    @GetMapping("/limit-reached")
    public ResponseEntity<Map<String, Object>> checkGuardLimit() {
        return ResponseEntity.ok(guardService.checkGuardLimit());
    }

    @GetMapping("/bridge-configured")
    public ResponseEntity<Map<String, Boolean>> checkBridgeConfigured() {
        return ResponseEntity.ok(guardService.checkBridgeConfigured());
    }

    @GetMapping("/limit-state-and-guard-count")
    public ResponseEntity<Map<String, Object>> getLimitStateAndGuardCount() {
        return ResponseEntity.ok(guardService.getLimitStateAndGuardCount());
    }

    @GetMapping("/guard-configured")
    public ResponseEntity<Map<String, Boolean>> checkGuardConfigured() {
        return ResponseEntity.ok(guardService.checkGuardConfigured());
    }
}