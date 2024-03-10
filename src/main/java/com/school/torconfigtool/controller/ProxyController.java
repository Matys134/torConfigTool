package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.ProxyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * This class is a Spring Boot controller for managing the proxy.
 */
@Controller
@RequestMapping("/proxy")
public class ProxyController {
    private final ProxyService proxyService;

    /**
     * Constructor for the ProxyController class.
     * @param proxyService the service that this controller will use to manage the proxy.
     */
    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    /**
     * Handles GET requests to the /proxy endpoint.
     * Adds the current proxy status to the model.
     *
     * @param model the model to add attributes to.
     * @return the name of the view to render.
     */
    @GetMapping
    public String proxyConfigurationForm(Model model) {
        try {
            model.addAttribute("proxyStatus", proxyService.isProxyRunning() ? "Running" : "Stopped");
        } catch (IOException e) {
            model.addAttribute("errorMessage", "An unexpected error occurred. Please check the logs for details.");
        }
        return "proxy-config";
    }

    /**
     * Handles POST requests to the /proxy/start endpoint.
     * Starts the proxy and adds relevant messages to the model.
     *
     * @param model the model to add attributes to.
     * @return the name of the view to render.
     */
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
            model.addAttribute("errorMessage", "An unexpected error occurred. Please check the logs for details.");
        }

        return "proxy-config";
    }

    /**
     * Handles POST requests to the /proxy/stop endpoint.
     * Stops the proxy, adding relevant messages to the model.
     *
     * @param model the model to add attributes to.
     * @return the name of the view to render.
     */
    @PostMapping("/stop")
    public String stopProxy(Model model) {
        try {
            if (!proxyService.stopProxy()) {
                model.addAttribute("errorMessage", "Failed to stop Tor Proxy.");
                return "proxy-config";
            }

            model.addAttribute("successMessage", "Tor Proxy stopped successfully!");

        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred. Please check the logs for details.");
        }

        return "proxy-config";
    }
}