package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

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
            // Create a temporary file to hold the updated configuration
            File tempFile = new File("torrc/local-torrc.temp"); // Temporary file path
            BufferedWriter tempWriter = new BufferedWriter(new FileWriter(tempFile));

            // Write the updated configuration to the temporary file
            tempWriter.write("Nickname " + relayNickname + "\n");
            tempWriter.write("BandwidthRate " + relayBandwidth + " KBytes\n");
            tempWriter.write("ORPort " + relayPort + "\n");
            tempWriter.write("ContactInfo " + relayContact + "\n");

            // Close the temporary file writer
            tempWriter.close();

            // Replace the existing configuration file with the temporary file
            File torrcFile = new File("torrc/local-torrc"); // Adjust the path to your torrc file
            if (torrcFile.exists()) {
                torrcFile.delete();
            }
            tempFile.renameTo(torrcFile);

            // Return true if configuration update is successful
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // Log and handle any exceptions that occur during the update
            return false;
        }
    }

    @PostMapping("/configure-bridge")
    public String configureBridge(@RequestParam int bridgePort,
                                  @RequestParam String bridgeEmail,
                                  @RequestParam String bridgeNickname,
                                  Model model) {
        // Implement logic to update the bridge configuration
        boolean configurationSuccess = updateBridgeConfiguration(
                bridgePort,
                bridgeEmail,
                bridgeNickname
        );

        if (configurationSuccess) {
            model.addAttribute("successMessage", "Bridge configuration updated successfully!");
        } else {
            model.addAttribute("errorMessage", "Failed to update bridge configuration.");
        }

        return "relay-config"; // Thymeleaf template name (relay-config.html)
    }

    private boolean updateBridgeConfiguration(
            int bridgePort,
            String bridgeEmail,
            String bridgeNickname
    ) {
        try {
            // Create a temporary file for bridge configuration
            File tempFile = new File("torrc/local-torrc-bridge.temp"); // Temporary file path for bridge configuration
            BufferedWriter tempWriter = new BufferedWriter(new FileWriter(tempFile));

            // Write bridge configuration lines to the temporary file
            tempWriter.write("BridgeRelay 1\n");
            tempWriter.write("ORPort " + bridgePort + "\n");
            tempWriter.write("ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy\n");
            tempWriter.write("ServerTransportListenAddr obfs4 0.0.0.0:" + bridgePort + "\n");
            tempWriter.write("ExtORPort auto\n");
            tempWriter.write("ContactInfo " + bridgeEmail + "\n");
            tempWriter.write("Nickname " + bridgeNickname + "\n");

            // Close the temporary file writer
            tempWriter.close();

            // Replace the existing bridge configuration file with the temporary file
            File bridgeConfigFile = new File("torrc/local-torrc-bridge"); // Adjust the path to your bridge config file
            if (bridgeConfigFile.exists()) {
                bridgeConfigFile.delete();
            }
            tempFile.renameTo(bridgeConfigFile);

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
