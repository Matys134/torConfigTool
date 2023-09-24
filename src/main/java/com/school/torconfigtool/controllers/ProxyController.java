package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/proxy")
public class ProxyController {

    @GetMapping
    public String torProxyConfigurationForm() {
        return "proxy-config"; // Thymeleaf template name (proxy-config.html)
    }

    @PostMapping("/configure")
    public String configureTorProxy(@RequestParam int proxyPort,
                                    @RequestParam String proxyBindAddress,
                                    @RequestParam int proxyControlPort,
                                    @RequestParam String proxyControlPassword,
                                    @RequestParam String proxyConfig,
                                    Model model) {
        // Handle form submission here
        // Update Tor Proxy configuration with the provided data

        // Redirect back to the configuration form with a success message
        model.addAttribute("successMessage", "Tor Proxy configured successfully!");
        return "proxy-config"; // Thymeleaf template name (proxy-config.html)
    }
}
