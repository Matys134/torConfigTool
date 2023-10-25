package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Controller
@RequestMapping("/relay")
public class RelayController {

    @GetMapping
    public String relayConfigurationForm() {
        return "relay-config"; // Thymeleaf template name (relay-config.html)
    }

    @PostMapping("/configure")
    public String configureRelay(@RequestParam String relayNickname,
                                 @RequestParam(required = false) Integer relayBandwidth,
                                 @RequestParam int relayPort,
                                 @RequestParam String relayContact,
                                 Model model) {
        try {
            // Define the path to the torrc file based on the relay nickname
            String torrcFileName = "local-torrc-" + relayNickname;
            String torrcFilePath = "torrc/guard/" + torrcFileName;

            // Check if the torrc file exists, create it if not
            if (!new File(torrcFilePath).exists()) {
                createTorrcFile(torrcFilePath, relayNickname, relayBandwidth, relayPort, relayContact);
            }

            // Restart the Tor service with the new configuration if necessary

            // Provide a success message
            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }

        return "relay-config"; // Thymeleaf template name (relay-config.html)
    }

    @PostMapping("/start")
    public String startRelay(Model model) {
        boolean startSuccess = startTorRelayService();

        if (startSuccess) {
            model.addAttribute("successMessage", "Tor Relay started successfully!");
        } else {
            model.addAttribute("errorMessage", "Failed to start Tor Relay service.");
        }

        return "relay-config"; // Redirect to the configuration page
    }

    @PostMapping("/stop")
    public String stopRelay(Model model) {
        boolean stopSuccess = stopTorRelayService();

        if (stopSuccess) {
            model.addAttribute("successMessage", "Tor Relay stopped successfully!");
        } else {
            model.addAttribute("errorMessage", "Failed to stop Tor Relay service.");
        }

        return "relay-config"; // Redirect to the configuration page
    }

    private boolean startTorRelayService() {
        try {
            // Execute a command to start the Tor service
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

    private boolean stopTorRelayService() {
        try {
            // Execute a command to stop the Tor service
            Process process = Runtime.getRuntime().exec("sudo systemctl stop tor");

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the stop was successful
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Log and handle any exceptions that occur during the stop
            return false;
        }
    }

    private void createTorrcFile(String filePath, String relayNickname, Integer relayBandwidth, int relayPort, String relayContact) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Nickname " + relayNickname);
            writer.newLine();
            if (relayBandwidth != null) {
                writer.write("BandwidthRate " + relayBandwidth + " KBytes");
                writer.newLine();
            }
            writer.write("ORPort " + relayPort);
            writer.newLine();
            writer.write("ContactInfo " + relayContact);
        }
    }
}
