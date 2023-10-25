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
import java.io.IOException;

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
                createTorrcFile(torrcFilePath, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname);
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

    private void createTorrcFile(String filePath, int bridgePort, int bridgeTransportListenAddr, String bridgeContact, String bridgeNickname) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("BridgeRelay 1");
            writer.newLine();
            writer.write("ORPort " + bridgePort);
            writer.newLine();
            writer.write("ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy");
            writer.newLine();
            writer.write("ServerTransportListenAddr obfs4 0.0.0.0:" + bridgeTransportListenAddr);
            writer.newLine();
            writer.write("ExtORPort auto");
            writer.newLine();
            writer.write("ContactInfo " + bridgeContact);
            writer.newLine();
            writer.write("Nickname " + bridgeNickname);
        }
    }
}
