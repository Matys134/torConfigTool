package com.school.torconfigtool.controllers;

import com.school.torconfigtool.config.TorrcConfigurator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.File;

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
            // Define the path to the torrc file based on the bridge nickname
            String torrcFileName = "local-torrc-bridge-" + bridgeNickname;
            String torrcFilePath = "torrc/bridge/" + torrcFileName;

            // Check if the torrc file exists, create it if not
            if (!new File(torrcFilePath).exists()) {
                // Define the Torrc configuration lines
                String[] torrcLines = {
                        "BridgeRelay 1",
                        "ORPort " + bridgePort,
                        "ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy",
                        "ServerTransportListenAddr obfs4 0.0.0.0:" + bridgeTransportListenAddr,
                        "ExtORPort auto",
                        "ContactInfo " + bridgeContact,
                        "Nickname " + bridgeNickname
                };
                boolean torrcCreated = TorrcConfigurator.createTorrcFile(torrcFilePath, torrcLines);
                if (torrcCreated) {
                    // Torrc file was successfully created
                    // You can add further actions or messages for success here
                    model.addAttribute("successMessage", "Tor Bridge configured successfully!");
                } else {
                    // Handle the error
                    // You can add specific error messages or take appropriate actions
                    model.addAttribute("errorMessage", "Failed to create Torrc file for the Bridge.");
                }
            }

            // Restart the Tor service with the new configuration if necessary

            // Provide a success message
            model.addAttribute("successMessage", "Tor Bridge configured successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to configure Tor Bridge.");
        }

        return "relay-config"; // Thymeleaf template name (bridge-config.html)
    }
}
