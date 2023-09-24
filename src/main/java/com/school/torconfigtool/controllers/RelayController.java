package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/relay")
public class RelayController {

    @GetMapping
    public String relayConfigurationForm() {
        return "relay-config"; // Thymeleaf template name (relay-config.html)
    }

    @PostMapping("/configure")
    public String configureRelay(@RequestParam String relayNickname,
                                 @RequestParam int relayBandwidth,
                                 @RequestParam int relayPort,
                                 @RequestParam int relayDirPort,
                                 @RequestParam String relayContact,
                                 @RequestParam String relayExitPolicy,
                                 @RequestParam String relayConfig,
                                 Model model) {
        // Handle form submission here
        // Update Tor Relay configuration with the provided data

        // Redirect back to the configuration form with a success message
        model.addAttribute("successMessage", "Tor Relay configured successfully!");
        return "relay-config"; // Thymeleaf template name (relay-config.html)
    }
}
