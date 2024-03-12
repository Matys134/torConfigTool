package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.ProxyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * This is a Spring MVC Controller that handles requests related to the Proxy.
 */
@Controller
@RequestMapping("/proxy")
public class ProxyController {

    private final ProxyService proxyService;

    /**
     * Constructor for the ProxyController class.
     * @param proxyService The service to handle operations related to the Proxy.
     */
    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    /**
     * Endpoint to display the proxy configuration form.
     * @param model The Model object to bind data to the view.
     * @return The name of the view to be rendered.
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
     * Endpoint to start the proxy.
     * @param model The Model object to bind data to the view.
     * @return The name of the view to be rendered.
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
     * Endpoint to stop the proxy.
     * @param model The Model object to bind data to the view.
     * @return The name of the view to be rendered.
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