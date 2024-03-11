package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.ProxyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/proxy")
public class ProxyController {

    private final ProxyService proxyService;

    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @GetMapping
    public String proxyConfigurationForm(Model model) {
        try {
            model.addAttribute("proxyStatus", proxyService.isProxyRunning() ? "Running" : "Stopped");
        } catch (IOException e) {
            System.err.println("Error during checking Tor Proxy status: " + e.getMessage());
            model.addAttribute("errorMessage", "An unexpected error occurred. Please check the logs for details.");
        }
        return "proxy-config";
    }

    @PostMapping("/start")
    public String startProxy(Model model) {
        try {
            String result = proxyService.configureAndStartProxy();
            if (result.equals("success")) {
                model.addAttribute("successMessage", "Tor Proxy started successfully!");
            } else {
                model.addAttribute("errorMessage", result);
            }
        } catch (Exception e) {
            System.err.println("Error during Tor Proxy configuration or start: " + e.getMessage());
            model.addAttribute("errorMessage", "An unexpected error occurred. Please check the logs for details.");
        }

        return "proxy-config";
    }

    @PostMapping("/stop")
    public String stopProxy(Model model) {
        try {
            if (!proxyService.stopProxy()) {
                model.addAttribute("errorMessage", "Failed to stop Tor Proxy.");
                return "proxy-config";
            }

            model.addAttribute("successMessage", "Tor Proxy stopped successfully!");

        } catch (Exception e) {
            System.err.println("Error during Tor Proxy stop: " + e.getMessage());
            model.addAttribute("errorMessage", "An unexpected error occurred. Please check the logs for details.");
        }

        return "proxy-config";
    }
}