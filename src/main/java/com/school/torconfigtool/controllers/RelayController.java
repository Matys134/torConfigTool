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
import java.util.ArrayList;
import java.util.List;

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
                                 @RequestParam String relayContact,
                                 Model model) {
        // Implement logic to update the Tor Relay configuration and restart the service
        boolean configurationSuccess = updateTorRelayConfiguration(
                relayNickname,
                relayBandwidth,
                relayPort,
                relayContact
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


    private boolean updateTorRelayConfiguration(
            String relayNickname,
            int relayBandwidth,
            int relayPort,
            String relayContact
    ) {
        try {
            // Read the existing Tor Relay configuration file (torrc)
            File torrcFile = new File("torrc/local-torrc"); // Adjust the path to your torrc file
            BufferedReader reader = new BufferedReader(new FileReader(torrcFile));
            String line;
            StringBuilder newTorrcContent = new StringBuilder();

            // Flags to check if lines exist
            boolean nicknameExists = false;
            boolean bandwidthRateExists = false;
            boolean orPortExists = false;
            boolean contactInfoExists = false;

            while ((line = reader.readLine()) != null) {
                // Modify the relevant configuration parameters based on user input
                if (line.startsWith("Nickname ")) {
                    newTorrcContent.append("Nickname ").append(relayNickname).append("\n");
                    nicknameExists = true;
                } else if (line.startsWith("BandwidthRate ")) {
                    newTorrcContent.append("BandwidthRate ").append(relayBandwidth).append(" KBytes\n");
                    bandwidthRateExists = true;
                } else if (line.startsWith("ORPort ")) {
                    newTorrcContent.append("ORPort ").append(relayPort).append("\n");
                    orPortExists = true;
                } else if (line.startsWith("ContactInfo ")) {
                    newTorrcContent.append("ContactInfo ").append(relayContact).append("\n");
                    contactInfoExists = true;
                } else {
                    newTorrcContent.append(line).append("\n");
                }
            }

            reader.close();

            // If the lines don't exist, create and append them
            if (!nicknameExists) {
                newTorrcContent.append("Nickname ").append(relayNickname).append("\n");
            }
            if (!bandwidthRateExists) {
                newTorrcContent.append("BandwidthRate ").append(relayBandwidth).append(" KBytes\n");
            }
            if (!orPortExists) {
                newTorrcContent.append("ORPort ").append(relayPort).append("\n");
            }
            if (!contactInfoExists) {
                newTorrcContent.append("ContactInfo ").append(relayContact).append("\n");
            }

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

    // Request password from user for sudo commands


}
