package com.school.torconfigtool.controllers;

import com.school.torconfigtool.config.ProxyConfigurator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/proxy")
public class ProxyController {

    @GetMapping
    public String proxyConfigurationForm() {
        return "proxy-config"; // Thymeleaf template name (proxy-config.html)
    }

    @PostMapping("/start")
    public String startProxy(Model model) {
        // Configure and start the Tor Proxy
        boolean configureSuccess = ProxyConfigurator.configureProxy();
        boolean startSuccess = ProxyConfigurator.startProxy();

        if (configureSuccess && startSuccess) {
            model.addAttribute("successMessage", "Tor Proxy started successfully!");
        } else {
            model.addAttribute("errorMessage", "Failed to start Tor Proxy.");
        }

        return "proxy-config"; // Redirect to the configuration page
    }
}
