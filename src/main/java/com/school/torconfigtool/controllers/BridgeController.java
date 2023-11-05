package com.school.torconfigtool.controllers;

import com.school.torconfigtool.config.TorrcConfigurator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/bridge")
public class BridgeController {

    @GetMapping
    public String bridgeConfigurationForm() {
        return "relay-config";
    }

    @PostMapping("/configure")
    public String configureBridge(@RequestParam int bridgePort,
                                  @RequestParam int bridgeTransportListenAddr,
                                  @RequestParam String bridgeContact,
                                  @RequestParam String bridgeNickname,
                                  Model model) {
        String torrcFileName = "local-torrc-bridge-" + bridgeNickname;
        String torrcDirectoryPath = "torrc/bridge/";
        Path torrcFilePath = Paths.get(torrcDirectoryPath, torrcFileName);

        try {
            if (!Files.exists(torrcFilePath)) {
                String[] torrcLines = getTorrcConfigLines(bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname);
                TorrcConfigurator.createTorrcFile(torrcFilePath.toString(), torrcLines);
            }
            model.addAttribute("successMessage", "Tor Bridge configured successfully!");
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Failed to create Torrc file for the Bridge: " + e.getMessage());
        }

        return "relay-config";
    }

    private String[] getTorrcConfigLines(int bridgePort, int bridgeTransportListenAddr, String bridgeContact, String bridgeNickname) {
        return new String[]{
                "BridgeRelay 1",
                "ORPort " + bridgePort,
                "ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy",
                "ServerTransportListenAddr obfs4 0.0.0.0:" + bridgeTransportListenAddr,
                "ExtORPort auto",
                "ContactInfo " + bridgeContact,
                "Nickname " + bridgeNickname
        };
    }
}
