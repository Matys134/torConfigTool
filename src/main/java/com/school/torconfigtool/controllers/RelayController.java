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
        try {
            // Define the path to the shell script
            String scriptPath = "shellScripts/configure-relay.sh";  // Update this path

            // Create a process builder for the script
            ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath,
                    relayNickname, String.valueOf(relayBandwidth), String.valueOf(relayPort), relayContact);

            // Start the process and wait for it to complete
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                model.addAttribute("successMessage", "Tor Relay configured and restarted successfully!");
            } else {
                model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to run configuration script.");
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

}
