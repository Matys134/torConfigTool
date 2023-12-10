package com.school.torconfigtool.controllers;

import com.school.torconfigtool.RelayUtils;
import com.school.torconfigtool.config.TorrcConfigurator;
import com.school.torconfigtool.models.BridgeRelayConfig;
import com.school.torconfigtool.service.RelayService;
import com.school.torconfigtool.service.TorrcFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/bridge")
public class BridgeController {

    private static final Logger logger = LoggerFactory.getLogger(BridgeController.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private static final String TORRC_FILE_PREFIX = "torrc-";
    private final RelayOperationsController relayOperationController;
    private final RelayService relayService;

    public BridgeController(RelayService relayService, RelayOperationsController relayOperationController) {
        this.relayService = relayService;
        this.relayOperationController = relayOperationController;
    }

    @GetMapping
    public String bridgeConfigurationForm() {
        return "relay-config";
    }

    @PostMapping("/configure")
    public String configureBridge(@RequestParam String bridgeType,
                                  @RequestParam(required = false) Integer bridgePort,
                                  @RequestParam(required = false) Integer bridgeTransportListenAddr,
                                  @RequestParam String bridgeContact,
                                  @RequestParam String bridgeNickname,
                                  @RequestParam(required = false) String webtunnelDomain,
                                  @RequestParam int bridgeControlPort,
                                  @RequestParam(required = false) String webtunnelUrl,
                                  @RequestParam(required = false) Integer webtunnelPort,
                                  @RequestParam(defaultValue = "false") boolean startBridgeAfterConfig,
                                  Model model) {
        try {
            //if bridgeport is null, check only if controlport is available and vice versa
            if (bridgePort == null && !relayService.isPortAvailable(bridgeNickname, bridgeControlPort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "relay-config";
            } else if (bridgeControlPort == 0 && !relayService.isPortAvailable(bridgeNickname, bridgePort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "relay-config";
            } else if (!relayService.arePortsAvailable(bridgeNickname, bridgePort, bridgeControlPort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "relay-config";
            }

            String torrcFileName = TORRC_FILE_PREFIX + bridgeNickname + "_bridge";
            Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

            if (RelayUtils.relayExists(bridgeNickname)) {
                model.addAttribute("errorMessage", "A relay with the same nickname already exists.");
                return "relay-config";
            }

            if (!RelayUtils.portsAreAvailable(bridgeNickname, bridgePort, bridgeControlPort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "relay-config";
            }

            BridgeRelayConfig config = createBridgeConfig(bridgeNickname, bridgePort, bridgeContact, bridgeControlPort);
            config.setBridgeType(bridgeType);
            config.setWebtunnelDomain(webtunnelDomain);
            config.setWebtunnelUrl(webtunnelUrl);
            config.setWebtunnelPort(webtunnelPort == null ? 0 : webtunnelPort.intValue());
            config.setEmail(bridgeContact); // Assume bridgeContact is email here
            if (!torrcFilePath.toFile().exists()) {
                TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
            }

            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }

        if (startBridgeAfterConfig) {
            try {
                relayOperationController.startRelay(bridgeNickname, "guard", model);
                model.addAttribute("successMessage", "Tor Relay configured and started successfully!");
            } catch (Exception e) {
                logger.error("Error starting Tor Relay", e);
                model.addAttribute("errorMessage", "Failed to start Tor Relay.");
            }
        }

        return "relay-config";
    }

    private BridgeRelayConfig createBridgeConfig(String bridgeNickname, int bridgePort, String bridgeContact, int bridgeControlPort) {
        BridgeRelayConfig config = new BridgeRelayConfig();
        config.setNickname(bridgeNickname);
        config.setOrPort(String.valueOf(bridgePort));
        config.setContact(bridgeContact);
        config.setControlPort(String.valueOf(bridgeControlPort));

        return config;
    }
}