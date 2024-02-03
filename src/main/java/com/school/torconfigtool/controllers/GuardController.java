package com.school.torconfigtool.controllers;

import com.school.torconfigtool.RelayUtils;
import com.school.torconfigtool.models.GuardRelayConfig;
import com.school.torconfigtool.service.RelayService;
import com.school.torconfigtool.service.TorrcFileCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/guard")
public class GuardController {

    private static final Logger logger = LoggerFactory.getLogger(GuardController.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private static final String TORRC_FILE_PREFIX = "torrc-";

    private final RelayService relayService;
    private final RelayOperationsController relayOperationController;


    public GuardController(RelayService relayService, RelayOperationsController relayOperationController) {
        this.relayService = relayService;
        this.relayOperationController = relayOperationController;
    }

    @GetMapping
    public String guardConfigurationForm(Model model) {
        logger.info("Relay configuration form requested");
        RelayUtils.checkRunningRelays();

        List<String> availableRelayTypes = RelayUtils.determineAvailableRelayTypes();
        model.addAttribute("availableRelayTypes", availableRelayTypes);

        return "setup";
    }

    @PostMapping("/configure")
    public String configureGuard(@RequestParam String relayNickname,
                                 @RequestParam int relayPort,
                                 @RequestParam String relayContact,
                                 @RequestParam int controlPort,
                                 @RequestParam(required = false) Integer relayBandwidth,
                                 @RequestParam(defaultValue = "false") boolean startRelayAfterConfig,
                                 Model model) {
        try {
            if (!relayService.arePortsAvailable(relayNickname, relayPort, controlPort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "setup";
            }

            String torrcFileName = TORRC_FILE_PREFIX + relayNickname + "_guard";
            Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

            if (RelayUtils.relayExists(relayNickname)) {
                model.addAttribute("errorMessage", "A relay with the same nickname already exists.");
                return "setup";
            }

            if (!RelayUtils.portsAreAvailable(relayNickname, relayPort, controlPort)) {
                model.addAttribute("errorMessage", "One or more ports are already in use.");
                return "setup";
            }

            GuardRelayConfig config = createGuardConfig(relayNickname, relayPort, relayContact, controlPort, relayBandwidth);
            if (!torrcFilePath.toFile().exists()) {
                TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
            }

            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }

        if (startRelayAfterConfig) {
            try {
                relayOperationController.startRelay(relayNickname, "guard", model);
                model.addAttribute("successMessage", "Tor Relay configured and started successfully!");
            } catch (Exception e) {
                logger.error("Error starting Tor Relay", e);
                model.addAttribute("errorMessage", "Failed to start Tor Relay.");
            }
        }

        return "setup";
    }

    private GuardRelayConfig createGuardConfig(String relayNickname, int relayPort, String relayContact, int controlPort, Integer relayBandwidth) {
        GuardRelayConfig config = new GuardRelayConfig();
        config.setNickname(relayNickname);
        config.setOrPort(String.valueOf(relayPort));
        config.setContact(relayContact);
        config.setControlPort(String.valueOf(controlPort));
        if (relayBandwidth != null) {
            config.setBandwidthRate(String.valueOf(relayBandwidth));
        }

        return config;
    }

    @GetMapping("/limit-reached")
    public ResponseEntity<Map<String, Boolean>> checkGuardLimit() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("guardLimitReached", relayService.getGuardCount() >= 8);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bridge-configured")
    public ResponseEntity<Map<String, Boolean>> checkBridgeConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("bridgeConfigured", relayService.getBridgeCount() > 0);
        return ResponseEntity.ok(response);
    }
}
