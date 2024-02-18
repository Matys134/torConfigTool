package com.school.torconfigtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling Onion Service related operations.
 */
@Controller
@RequestMapping("/onion-service")
public class OnionController {

    private static final Logger logger = LoggerFactory.getLogger(OnionController.class);
    private final TorConfigurationService torConfigurationService;
    private final OnionService onionService;
    TorConfiguration torConfiguration = new TorConfiguration();

    /**
     * Constructor for OnionController.
     * Initializes the required services and checks if the hiddenServiceDirs directory exists.
     * If it doesn't exist, it attempts to create it.
     */
    @Autowired
    public OnionController(TorConfigurationService torConfigurationService, OnionService onionService, OnionService onionService1) {
        this.torConfigurationService = torConfigurationService;
        this.onionService = onionService1;
        List<String> onionServicePorts = onionService.getAllOnionServicePorts();

        // Set the hiddenServicePort here if it's not being set elsewhere
        if (!onionServicePorts.isEmpty()) {
            torConfiguration.setHiddenServicePort(onionServicePorts.getFirst());
        }

        // Check if hiddenServiceDirs directory exists, if not, create it
        String hiddenServiceDirsPath = System.getProperty("user.dir") + "/onion/hiddenServiceDirs";
        File hiddenServiceDirs = new File(hiddenServiceDirsPath);
        if (!hiddenServiceDirs.exists()) {
            boolean dirCreated = hiddenServiceDirs.mkdirs();
            if (!dirCreated) {
                logger.error("Failed to create directory: " + hiddenServiceDirsPath);
            }
        }
    }

    /**
     * Handles GET requests to the base path.
     * Returns the setup page with the current onion configurations and hostnames.
     */
    @GetMapping
    public String onionServiceConfigurationForm(Model model) {
        Map<String, String> hostnames = onionService.getCurrentHostnames();
        List<TorConfiguration> onionConfigs = torConfigurationService.readTorConfigurations();
        String hostname = onionService.readHostnameFile(Integer.parseInt(torConfiguration.getHiddenServicePort())); // Assuming port 80 for this example

        model.addAttribute("hostname", hostname);
        model.addAttribute("onionConfigs", onionConfigs);
        model.addAttribute("hostnames", hostnames);

        return "setup"; // The name of the Thymeleaf template to render
    }


    @GetMapping("/current-hostnames")
    @ResponseBody
    public Map<String, String> getCurrentHostnames() {
        return onionService.getCurrentHostnames();
    }


    @PostMapping("/configure")
    public String configureOnionService(@RequestParam int onionServicePort, Model model) {
        try {
            onionService.configureOnionService(onionServicePort);
            model.addAttribute("successMessage", "Tor Onion Service configured successfully!");
        } catch (IOException e) {
            logger.error("Error configuring Tor Onion Service", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Onion Service.");
        }
        return "setup";
    }

    /**
     * Handles POST requests to /start.
     * Starts the onion service.
     */
    @PostMapping("/start")
    public String startOnionService(Model model) {
        boolean startSuccess = onionService.startTorOnion();
        if (startSuccess) {
            model.addAttribute("successMessage", "Tor Onion Service started successfully!");
        } else {
            model.addAttribute("errorMessage", "Failed to start Tor Onion Service.");
        }
        return "setup";
    }


    @PostMapping("/refresh-nginx")
    public ResponseEntity<Void> refreshNginx() {
        try {
            onionService.refreshNginx();
            return ResponseEntity.ok().build();
        } catch (IOException | InterruptedException e) {
            logger.error("Error refreshing Nginx", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/onion-configured")
    public ResponseEntity<Map<String, Boolean>> checkOnionConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        boolean isOnionConfigured = onionService.checkOnionConfigured();
        response.put("onionConfigured", isOnionConfigured);
        return ResponseEntity.ok(response);
    }
}