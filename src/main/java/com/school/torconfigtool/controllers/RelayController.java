package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.RelayData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

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
            // Define the path to the shell script
            String scriptPath = "shellScripts/configure-relay.sh";  // Update this path

            // Create a process builder for the script
            ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath,
                    relayNickname, relayBandwidth != null ? String.valueOf(relayBandwidth) : "", String.valueOf(relayPort), relayContact);

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

    @GetMapping("/relay-traffic")
    public String showRelayTraffic(Model model) {
        // Create a RestTemplate to make an HTTP GET request to your API
        RestTemplate restTemplate = new RestTemplate();

        // Replace with the actual URL of your relay data API
        String relayDataUrl = "http://192.168.2.117:8081/api/relay-data";

        // Send an HTTP GET request to fetch relay data
        RelayData[] relayData = restTemplate.getForObject(relayDataUrl, RelayData[].class);

        // Add relay data to the model for rendering in the Thymeleaf template
        model.addAttribute("relayData", relayData);

        return "data"; // Thymeleaf template name
    }

}
