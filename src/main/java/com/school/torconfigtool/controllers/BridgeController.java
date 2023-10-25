package com.school.torconfigtool.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/bridge")
public class BridgeController {

    @GetMapping
    public String bridgeConfigurationForm() {
        return "relay-config"; // Thymeleaf template name (bridge-config.html)
    }

    @PostMapping("/configure")
    public String configureBridge(@RequestParam int bridgePort,
                                  @RequestParam int bridgeTransportListenAddr,
                                  @RequestParam String bridgeContact,
                                  @RequestParam String bridgeNickname,
                                  Model model) {
        try {
            // Define the path to the shell script
            String scriptPath = "shellScripts/configure-bridge.sh";

            // Create a process builder for the script
            ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath,
                    String.valueOf(bridgePort), String.valueOf(bridgeTransportListenAddr), bridgeContact, bridgeNickname);

            // Start the process and wait for it to complete
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                model.addAttribute("successMessage", "Tor Bridge configured and restarted successfully!");
            } else {
                model.addAttribute("errorMessage", "Failed to configure Tor Bridge.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to run configuration script.");
        }

        return "relay-config"; // Thymeleaf template name (bridge-config.html)
    }


}
