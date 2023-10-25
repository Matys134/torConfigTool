package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Controller
@RequestMapping("/onion-service")
public class OnionServiceController {

    @GetMapping
    public String onionServiceConfigurationForm() {
        return "onion-service-config"; // Thymeleaf template name (onion-service-config.html)
    }

    @PostMapping("/configure")
    public String configureOnionService(@RequestParam int onionServicePort, Model model) {
        try {
            // Define the path to the torrc file for the onion service
            String torrcFileName = "local-torrc-onion-service";
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

        return "onion-service-config"; // Redirect to the configuration page
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

        return "onion-service-config"; // Redirect to the configuration page
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("HiddenServiceDir /var/lib/tor/my_website/");
            writer.newLine();
            writer.write("HiddenServicePort " + onionServicePort + " 127.0.0.1:80");
        }
    }
}
