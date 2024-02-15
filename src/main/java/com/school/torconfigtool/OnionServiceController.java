package com.school.torconfigtool;

import com.school.torconfigtool.nginx.service.NginxConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/onion-service")
public class OnionServiceController {


    private static final Logger logger = LoggerFactory.getLogger(OnionServiceController.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private final TorConfigurationService torConfigurationService;
    private final List<String> onionServicePorts;
    private final TorService torService;
    private final NginxConfigService nginxConfigService;

    TorConfiguration torConfiguration = new TorConfiguration();

    private final RelayOperationsController relayOperationController;

    @Autowired
    public OnionServiceController(TorConfigurationService torConfigurationService, RelayOperationsController relayOperationController, TorService torService, NginxConfigService nginxConfigService) {
        this.torConfigurationService = torConfigurationService;
        this.relayOperationController = relayOperationController; // Initialize the field
        this.onionServicePorts = getAllOnionServicePorts();
        this.torService = torService;
        this.nginxConfigService = nginxConfigService;

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

    private List<String> getAllOnionServicePorts() {
        List<String> ports = new ArrayList<>();
        File torrcDirectory = new File(TORRC_DIRECTORY_PATH);
        File[] torrcFiles = torrcDirectory.listFiles((dir, name) -> name.endsWith("_onion"));

        if (torrcFiles != null) {
            for (File file : torrcFiles) {
                String fileName = file.getName();
                String port = fileName.substring(fileName.indexOf('-') + 1, fileName.indexOf('_'));
                ports.add(port);
            }
        }

        return ports;
    }

    @GetMapping
    public String onionServiceConfigurationForm(Model model) {
        System.out.println("OnionServiceConfigurationForm called");
        List<TorConfiguration> onionConfigs = torConfigurationService.readTorConfigurations();
        Map<String, String> hostnames = new HashMap<>();

        for (String port : onionServicePorts) {
            String hostname = readHostnameFile(Integer.parseInt(port));
            hostnames.put(port, hostname);
        }
        String hostname = readHostnameFile(Integer.parseInt(torConfiguration.getHiddenServicePort())); // Assuming port 80 for this example
        model.addAttribute("hostname", hostname);

        model.addAttribute("onionConfigs", onionConfigs);
        model.addAttribute("hostnames", hostnames);

        return "setup"; // The name of the Thymeleaf template to render
    }


    @GetMapping("/current-hostnames")
    @ResponseBody
    public Map<String, String> getCurrentHostnames() {
        logger.info("Inside getCurrentHostnames method");
        Map<String, String> hostnames = new HashMap<>();
        for (String hiddenServicePortString : onionServicePorts) {
            logger.info("Hidden Service Port: {}", hiddenServicePortString);

            if (hiddenServicePortString != null) {
                String hostname = readHostnameFile(Integer.parseInt(hiddenServicePortString));
                logger.info("Fetched Hostname: {}", hostname);
                hostnames.put(hiddenServicePortString, hostname);
            } else {
                logger.warn("Hidden service port is null");
                hostnames.put(null, "Hidden service port is null");
            }
        }
        return hostnames;
    }


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
                torService.createTorrcFile(pathToFile, onionServicePort);
                nginxConfigService.generateNginxConfig(onionServicePort);

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

    private String readHostnameFile(int port) {
        return torService.readHostnameFile(port);
    }


    @PostMapping("/start")
    public String startOnionService(Model model) {
        boolean startSuccess = startTorOnionService();
        if (startSuccess) {
            model.addAttribute("successMessage", "Tor Onion Service started successfully!");
        } else {
            model.addAttribute("errorMessage", "Failed to start Tor Onion Service.");
        }
        return "setup";
    }

    private boolean startTorOnionService() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "systemctl", "start", "tor");
            Process process = processBuilder.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the start was successful
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.error("Error starting Tor Onion Service", e);
            // Log and handle any exceptions that occur during the start
            return false;
        }
    }

    @GetMapping("/onion-configured")
    public ResponseEntity<Map<String, Boolean>> checkOnionConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        // Logic to check if an onion service is configured
        boolean isOnionConfigured = !onionServicePorts.isEmpty();
        response.put("onionConfigured", isOnionConfigured);
        return ResponseEntity.ok(response);
    }
}
