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
    public String configureBridge(@RequestParam String bridgeType,
                                  @RequestParam(required = false) int bridgePort,
                                  @RequestParam(required = false) int bridgeTransportListenAddr,
                                  @RequestParam(required = false) String bridgeContact,
                                  @RequestParam(required = false) String bridgeNickname,
                                  @RequestParam(required = false) String webtunnelDomain,
                                  Model model) {
        String torrcFileName = "local-torrc-bridge-" + (bridgeNickname != null ? bridgeNickname : "");
        String torrcDirectoryPath = "torrc/bridge/";
        Path torrcFilePath = Paths.get(torrcDirectoryPath, torrcFileName);

        try {
            if (!Files.exists(torrcFilePath)) {
                String[] torrcLines = getTorrcConfigLines(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain);
                TorrcConfigurator.createTorrcFile(torrcFilePath.toString(), torrcLines);
            }
            model.addAttribute("successMessage", "Tor Bridge configured successfully!");
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Failed to create Torrc file for the Bridge: " + e.getMessage());
        }

        return "relay-config";
    }

    private String[] getTorrcConfigLines(String bridgeType, int bridgePort, int bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain) {
        if ("bridge".equals(bridgeType)) {
            return new String[]{
                    "BridgeRelay 1",
                    "ORPort " + bridgePort,
                    "ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy",
                    "ServerTransportListenAddr obfs4 0.0.0.0:" + bridgeTransportListenAddr,
                    "ExtORPort auto",
                    "ContactInfo " + bridgeContact,
                    "Nickname " + bridgeNickname,
            };
        } else if ("webtunnel".equals(bridgeType)) {
            return new String[]{
                    "BridgeRelay 1",
                    "ORPort 127.0.0.1:auto",
                    "AssumeReachable 1",
                    "ServerTransportPlugin webtunnel exec /usr/local/bin/webtunnel",
                    "ServerTransportListenAddr webtunnel 127.0.0.1:15000",
                    "ServerTransportOptions webtunnel url=" + webtunnelDomain,
                    "ExtORPort auto",
                    "ContactInfo " + bridgeContact,
                    "Nickname " + bridgeNickname,
                    "SocksPort 0",
            };
        } else {
            return new String[0];
        }
    }
}
