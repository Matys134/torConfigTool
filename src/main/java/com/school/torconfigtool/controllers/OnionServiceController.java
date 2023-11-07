package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.TorConfiguration;
import com.school.torconfigtool.service.TorConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/onion-service")
public class OnionServiceController {

    private final TorConfigurationService torConfigurationService;

    @Autowired
    public OnionServiceController(TorConfigurationService torConfigurationService) {
        this.torConfigurationService = torConfigurationService;
    }

    @GetMapping
    public String onionServiceConfigurationForm(Model model) {
        List<TorConfiguration> onionConfigs = torConfigurationService.readTorConfigurations("onion");
        Map<String, String> hostnames = new HashMap<>();

        for (TorConfiguration config : onionConfigs) {
            String hostname = readHostnameFile(Integer.parseInt(config.getHiddenServicePort()));
            hostnames.put(config.getHiddenServicePort(), hostname);
        }
        String hostname = readHostnameFile(80); // Assuming port 80 for this example
        model.addAttribute("hostname", hostname);

        model.addAttribute("onionConfigs", onionConfigs);
        model.addAttribute("hostnames", hostnames);
        return "relay-config"; // The name of the Thymeleaf template to render
    }

    @GetMapping("/current-hostname")
    @ResponseBody
    public String getCurrentHostname() {
        return readHostnameFile(80); // Or however you determine the correct port
    }


    @PostMapping("/configure")
    public String configureOnionService(@RequestParam int onionServicePort, Model model) {
        try {
            // Define the path to the torrc file for the onion service
            String torrcFileName = "local-torrc-onion" + onionServicePort;
            String torrcFilePath = "torrc/onion/" + torrcFileName;

            // Check if the torrc file exists, create it if not
            if (!new File(torrcFilePath).exists()) {
                createTorrcFile(torrcFilePath, onionServicePort);
            }

            model.addAttribute("successMessage", "Tor Onion Service configured successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to configure Tor Onion Service.");
        }

        return "relay-config"; // Redirect to the configuration page
    }

    @PostMapping("/start")
    public String startOnionService(Model model) {
        // Configure and start the Tor Onion Service

        // You can call the TorOnionServiceConfigurator.configureTorOnionService() method here
        // to configure the Onion Service based on the previously created torrc file.

        boolean startSuccess = startTorOnionService();

        if (startSuccess) {
            model.addAttribute("successMessage", "Tor Onion Service started successfully!");
        } else {
            model.addAttribute("errorMessage", "Failed to start Tor Onion Service.");
        }

        return "relay-config"; // Redirect to the configuration page
    }

    private boolean startTorOnionService() {
        try {
            // Execute a command to start the Tor Onion Service
            Process process = Runtime.getRuntime().exec("sudo systemctl start tor");

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the start was successful
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Log and handle any exceptions that occur during the start
            return false;
        }
    }

    private void createTorrcFile(String filePath, int onionServicePort) throws IOException {
        try (BufferedWriter torrcWritter = new BufferedWriter(new FileWriter(filePath))) {
            String currentDirectory = System.getProperty("user.dir");
            String hiddenServiceDirs = currentDirectory + "/onion/hiddenServiceDirs";

            File wwwDir = new File(currentDirectory + "/onion/www");
            if (!wwwDir.exists()) {
                wwwDir.mkdirs();
            }

            File serviceDir = new File(wwwDir, "service-" + onionServicePort);
            serviceDir.mkdirs();

            torrcWritter.write("HiddenServiceDir " + hiddenServiceDirs + "/onion-service-" + onionServicePort + "/");
            torrcWritter.newLine();
            torrcWritter.write("HiddenServicePort " + onionServicePort + " 127.0.0.1:" + onionServicePort);

            File indexHtml = new File(serviceDir, "index.html");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexHtml))) {
                writer.write("<html><body><h1>Test Onion Service</h1></body></html>");
            }
        }
    }

    private String readHostnameFile(int port) {
        // Adjust the file path as needed
        Path path = Paths.get("onion/hiddenServiceDirs/onion-service-" + port + "/hostname");
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            return "Unable to read hostname file";
        }
    }
}
