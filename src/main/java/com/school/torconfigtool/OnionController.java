package com.school.torconfigtool;

import com.school.torconfigtool.service.NginxService;
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
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private final TorConfigurationService torConfigurationService;
    private final NginxService nginxService;
    private final List<String> onionServicePorts;
    private final OnionService onionService;
    TorConfiguration torConfiguration = new TorConfiguration();
    private final RelayOperationsController relayOperationController;

    /**
     * Constructor for OnionController.
     * Initializes the required services and checks if the hiddenServiceDirs directory exists.
     * If it doesn't exist, it attempts to create it.
     */
    @Autowired
    public OnionController(TorConfigurationService torConfigurationService, NginxService nginxService, OnionService onionService, OnionService onionService1, RelayOperationsController relayOperationController) {
        this.torConfigurationService = torConfigurationService;
        this.nginxService = nginxService;
        this.onionService = onionService1;
        this.relayOperationController = relayOperationController; // Initialize the field
        this.onionServicePorts = onionService.getAllOnionServicePorts();

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
        System.out.println("OnionServiceConfigurationForm called");
        List<TorConfiguration> onionConfigs = torConfigurationService.readTorConfigurations();
        Map<String, String> hostnames = new HashMap<>();

        for (String port : onionServicePorts) {
            String hostname = onionService.readHostnameFile(Integer.parseInt(port));
            hostnames.put(port, hostname);
        }
        String hostname = onionService.readHostnameFile(Integer.parseInt(torConfiguration.getHiddenServicePort())); // Assuming port 80 for this example
        model.addAttribute("hostname", hostname);

        model.addAttribute("onionConfigs", onionConfigs);
        model.addAttribute("hostnames", hostnames);

        return "setup"; // The name of the Thymeleaf template to render
    }

    /**
     * Handles GET requests to /current-hostnames.
     * Returns a map of current hostnames.
     */
    @GetMapping("/current-hostnames")
    @ResponseBody
    public Map<String, String> getCurrentHostnames() {
        logger.info("Inside getCurrentHostnames method");
        Map<String, String> hostnames = new HashMap<>();
        for (String hiddenServicePortString : onionServicePorts) {
            logger.info("Hidden Service Port: {}", hiddenServicePortString);

            if (hiddenServicePortString != null) {
                String hostname = onionService.readHostnameFile(Integer.parseInt(hiddenServicePortString));
                logger.info("Fetched Hostname: {}", hostname);
                hostnames.put(hiddenServicePortString, hostname);
            } else {
                logger.warn("Hidden service port is null");
                hostnames.put(null, "Hidden service port is null");
            }
        }
        return hostnames;
    }

    /**
     * Handles POST requests to /configure.
     * Configures the onion service for the given port.
     */
    @PostMapping("/configure")
    public String configureOnionService(@RequestParam int onionServicePort, Model model) {
        // Check port availability before configuring the onion service
        if (!RelayUtils.isPortAvailable("torrc-" + onionServicePort + "_onion", onionServicePort)) {
            model.addAttribute("errorMessage", "Port is not available.");
            return "setup";
        }

        try {
            String pathToFile = TORRC_DIRECTORY_PATH + "torrc-" + onionServicePort + "_onion";
            if (!new File(pathToFile).exists()) {
                onionService.createTorrcFile(pathToFile, onionServicePort);
                nginxService.generateNginxConfig(onionServicePort);

                // Restart nginx
                relayOperationController.reloadNginx();
            }
            torConfiguration.setHiddenServicePort(String.valueOf(onionServicePort));
            logger.info("Hidden Service Port set to: {}", onionServicePort);
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

    /**
     * Handles POST requests to /refresh-nginx.
     * Refreshes the Nginx service.
     */
    @PostMapping("/refresh-nginx")
    public ResponseEntity<Void> refreshNginx() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "reload", "nginx");
            Process process = processBuilder.start();
            process.waitFor();
            return ResponseEntity.ok().build();
        } catch (IOException | InterruptedException e) {
            logger.error("Error refreshing Nginx", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handles GET requests to /onion-configured.
     * Checks if an onion service is configured.
     */
    @GetMapping("/onion-configured")
    public ResponseEntity<Map<String, Boolean>> checkOnionConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        boolean isOnionConfigured = !onionServicePorts.isEmpty();
        response.put("onionConfigured", isOnionConfigured);
        return ResponseEntity.ok(response);
    }
}