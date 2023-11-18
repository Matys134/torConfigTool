package com.school.torconfigtool.controllers;

import com.school.torconfigtool.config.ProxyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/proxy")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    @GetMapping
    public String proxyConfigurationForm() {
        return "proxy-config"; // Thymeleaf template name (proxy-config.html)
    }

    @PostMapping("/start")
    public String startProxy(Model model) {
        try {
            boolean configureSuccess = ProxyConfigurator.configureProxy();
            if (configureSuccess) {
                boolean startSuccess = ProxyConfigurator.startProxy();
                if (startSuccess) {
                    model.addAttribute("successMessage", "Tor Proxy started successfully!");
                } else {
                    model.addAttribute("errorMessage", "Failed to start Tor Proxy.");
                }
            } else {
                model.addAttribute("errorMessage", "Failed to configure Tor Proxy.");
            }
        } catch (Exception e) {
            logger.error("Error during Tor Proxy configuration or start", e);
            model.addAttribute("errorMessage", "An unexpected error occurred. Please check the logs for details.");
        }

        return "proxy-config"; // Redirect to the configuration page
    }
}
