package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

@Controller
@RequestMapping("/relay")
public class RelayController {

    @GetMapping
    public String relayConfigurationForm() {
        return "relay-config"; // Thymeleaf template name (relay-config.html)
    }

    @PostMapping("/configure")
    public String configureRelay(@RequestParam String relayNickname,
                                 @RequestParam int relayBandwidth,
                                 @RequestParam int relayPort,
                                 @RequestParam int relayDirPort,
                                 @RequestParam String relayContact,
                                 @RequestParam String relayExitPolicy,
                                 @RequestParam String relayConfig,
                                 Model model) {
        // Implement logic to update the Tor Relay configuration and restart the service
        boolean configurationSuccess = updateTorRelayConfiguration(
                relayNickname,
                relayBandwidth,
                relayPort,
                relayDirPort,
                relayContact,
                relayExitPolicy,
                relayConfig
        );

        if (configurationSuccess) {
            boolean restartSuccess = restartTorRelayService();

            if (restartSuccess) {
                model.addAttribute("successMessage", "Tor Relay configured and restarted successfully!");
            } else {
                model.addAttribute("errorMessage", "Failed to restart Tor Relay service. Please restart it manually.");
            }
        } else {
            model.addAttribute("errorMessage", "Failed to update Tor Relay configuration.");
        }

        return "relay-config"; // Thymeleaf template name (relay-config.html)
    }

    private boolean updateTorRelayConfiguration(
            String relayNickname,
            int relayBandwidth,
            int relayPort,
            int relayDirPort,
            String relayContact,
            String relayExitPolicy,
            String relayConfig
    ) {
        try {
            // Read the existing Tor Relay configuration file (torrc)
            File torrcFile = new File("/etc/tor/torrc"); // Adjust the path to your torrc file
            BufferedReader reader = new BufferedReader(new FileReader(torrcFile));
            String line;
            StringBuilder newTorrcContent = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                // Modify the relevant configuration parameters based on user input
                if (line.startsWith("Nickname ")) {
                    newTorrcContent.append("Nickname ").append(relayNickname).append("\n");
                } else if (line.startsWith("BandwidthRate ")) {
                    newTorrcContent.append("BandwidthRate ").append(relayBandwidth).append(" KBytes\n");
                } else if (line.startsWith("ORPort ")) {
                    newTorrcContent.append("ORPort ").append(relayPort).append("\n");
                } else if (line.startsWith("DirPort ")) {
                    newTorrcContent.append("DirPort ").append(relayDirPort).append("\n");
                } else if (line.startsWith("ContactInfo ")) {
                    newTorrcContent.append("ContactInfo ").append(relayContact).append("\n");
                } else {
                    newTorrcContent.append(line).append("\n");
                }
            }

            // Add additional configuration (if provided)
            if (!relayExitPolicy.isEmpty()) {
                newTorrcContent.append(relayExitPolicy).append("\n");
            }
            if (!relayConfig.isEmpty()) {
                newTorrcContent.append(relayConfig).append("\n");
            }

            reader.close();

            // Write the updated configuration back to the torrc file
            BufferedWriter writer = new BufferedWriter(new FileWriter(torrcFile));
            writer.write(newTorrcContent.toString());
            writer.close();

            // Return true if configuration update is successful
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // Log and handle any exceptions that occur during the update
            return false;
        }
    }

    private boolean restartTorRelayService() {
        try {
            // Execute a command to restart the Tor service
            Process process = Runtime.getRuntime().exec("sudo systemctl restart tor");

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Check the exit code to determine if the restart was successful
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Log and handle any exceptions that occur during the restart
            return false;
        }
    }
}
