package com.school.torconfigtool.controllers;

import com.school.torconfigtool.configurations.TorOnionServiceConfigurator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/onion-service")
public class OnionServiceController {

    @GetMapping
    public String onionServiceConfigurationForm() {
        return "onion-service-config"; // Thymeleaf template name (onion-service-config.html)
    }

    @PostMapping("/start")
    public String startOnionService(Model model) {
        // Configure and start the Tor Onion Service
        boolean configureSuccess = TorOnionServiceConfigurator.configureTorOnionService();
        boolean startSuccess = TorOnionServiceConfigurator.startTorOnionService();

        if (configureSuccess && startSuccess) {
            model.addAttribute("successMessage", "Tor Onion Service started successfully!");
        } else {
            model.addAttribute("errorMessage", "Failed to start Tor Onion Service.");
        }

        return "onion-service-config"; // Redirect to the configuration page
    }
}
