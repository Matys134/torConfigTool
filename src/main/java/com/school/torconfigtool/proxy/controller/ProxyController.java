package com.school.torconfigtool.proxy.controller;

import com.school.torconfigtool.proxy.config.ProxyFileCreator;
import com.school.torconfigtool.proxy.util.ProxyStarter;
import com.school.torconfigtool.proxy.config.ProxyConfigurator;
import com.school.torconfigtool.util.IpAddressRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * This class is a controller for managing the Tor Proxy.
 * It provides endpoints for starting, stopping, and checking the status of the proxy.
 */
@Controller
@RequestMapping("/proxy")
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final ProxyConfigurator proxyConfigurator;

    /**
     * Constructor for ProxyController.
     * Initializes the ProxyConfigurator with a new ProxyFileCreator, ProxyStarter, and IpAddressRetriever.
     */
    public ProxyController() {
        this.proxyConfigurator = new ProxyConfigurator(new ProxyFileCreator(), new ProxyStarter(), new IpAddressRetriever());
    }

    /**
     * Endpoint for getting the proxy configuration form.
     * It checks if the proxy is running and adds the status to the model.
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping
    public String proxyConfigurationForm(Model model) {
        try {
            model.addAttribute("proxyStatus", proxyConfigurator.isProxyRunning() ? "Running" : "Stopped");
        } catch (IOException e) {
            logger.error("Error during checking Tor Proxy status", e);
            model.addAttribute("errorMessage", "An unexpected error occurred. Please check the logs for details.");
        }
        return "proxy-config";
    }

    /**
     * Endpoint for starting the proxy.
     * It configures and starts the proxy, adding success or error messages to the model.
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @PostMapping("/start")
    public String startProxy(Model model) {
        try {
            logger.info("Configuring Tor Proxy...");
            if (!proxyConfigurator.configureProxy()) {
                logger.error("Failed to configure Tor Proxy.");
                model.addAttribute("errorMessage", "Failed to configure Tor Proxy.");
                return "proxy-config";
            }

            logger.info("Starting Tor Proxy...");
            if (!proxyConfigurator.startProxy()) {
                logger.error("Failed to start Tor Proxy.");
                model.addAttribute("errorMessage", "Failed to start Tor Proxy.");
                return "proxy-config";
            }

            model.addAttribute("successMessage", "Tor Proxy started successfully!");

        } catch (Exception e) {
            logger.error("Error during Tor Proxy configuration or start", e);
            model.addAttribute("errorMessage", "An unexpected error occurred. Please check the logs for details.");
        }

        return "proxy-config";
    }

    /**
     * Endpoint for stopping the proxy.
     * It stops the proxy and adds success or error messages to the model.
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @PostMapping("/stop")
    public String stopProxy(Model model) {
        try {
            if (!proxyConfigurator.stopProxy()) {
                model.addAttribute("errorMessage", "Failed to stop Tor Proxy.");
                return "proxy-config";
            }

            model.addAttribute("successMessage", "Tor Proxy stopped successfully!");

        } catch (Exception e) {
            logger.error("Error during Tor Proxy stop", e);
            model.addAttribute("errorMessage", "An unexpected error occurred. Please check the logs for details.");
        }

        return "proxy-config";
    }
}