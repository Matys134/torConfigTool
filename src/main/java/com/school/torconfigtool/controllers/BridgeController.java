package com.school.torconfigtool.controllers;

import com.school.torconfigtool.config.TorrcConfigurator;
import com.school.torconfigtool.service.RelayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/bridge")
public class BridgeController {

    private static final Logger logger = LoggerFactory.getLogger(BridgeController.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/bridge/";

    private final RelayService relayService;
    public BridgeController(RelayService relayService) {
        this.relayService = relayService;
    }

    @GetMapping
    public String bridgeConfigurationForm() {
        return "relay-config";
    }
    @PostMapping("/configure")
    public String configureBridge(@RequestParam String bridgeType,
                                  @RequestParam(required = false) Integer bridgePort,
                                  @RequestParam(required = false) Integer bridgeTransportListenAddr,
                                  @RequestParam(required = false) String bridgeContact,
                                  @RequestParam(required = false) String bridgeNickname,
                                  @RequestParam(required = false) String webtunnelDomain,
                                  Model model) {
        try {
            if (!relayService.isPortAvailable(bridgeType, bridgePort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "relay-config";
            }

            String torrcFileName = "torrc-" + (bridgeNickname != null ? bridgeNickname : "");
            Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

            if (!Files.exists(torrcFilePath)) {
                String[] torrcLines = getTorrcConfigLines(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain);
                TorrcConfigurator.createTorrcFile(torrcFilePath.toString(), torrcLines);
            }
            model.addAttribute("successMessage", "Tor Bridge configured successfully!");
        } catch (Exception e) {
            // Add appropriate exception handling (e.g., logging, displaying an error message)
            logger.error("Error configuring Tor Bridge", e);
            model.addAttribute("errorMessage", "Error configuring Tor Bridge. Please check the logs for details.");
        }

        return "relay-config";
    }


    private String[] getTorrcConfigLines(String bridgeType, int bridgePort, int bridgeTransportListenAddr, String bridgeContact, String bridgeNickname, String webtunnelDomain) {
        // Use constants or enums instead of hard-coded strings
        String bridgeRelayOption = "BridgeRelay 1";
        String extORPortOption = "ExtORPort auto";
        String contactInfoOption = "ContactInfo " + bridgeContact;
        String nicknameOption = "Nickname " + bridgeNickname;

        if ("bridge".equals(bridgeType)) {
            return new String[]{
                    bridgeRelayOption,
                    "ORPort " + bridgePort,
                    "ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy",
                    "ServerTransportListenAddr obfs4 0.0.0.0:" + bridgeTransportListenAddr,
                    extORPortOption,
                    contactInfoOption,
                    nicknameOption
            };
        } else if ("webtunnel".equals(bridgeType)) {
            return new String[]{
                    bridgeRelayOption,
                    "ORPort 127.0.0.1:auto",
                    "AssumeReachable 1",
                    "ServerTransportPlugin webtunnel exec /usr/local/bin/webtunnel",
                    "ServerTransportListenAddr webtunnel 127.0.0.1:15000",
                    "ServerTransportOptions webtunnel url=" + webtunnelDomain,
                    extORPortOption,
                    contactInfoOption,
                    nicknameOption,
                    "SocksPort 0"
            };
        } else {
            return new String[0];
        }
    }
}
