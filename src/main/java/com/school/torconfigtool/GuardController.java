package com.school.torconfigtool;

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


    public GuardController(RelayService relayService) {
        this.relayService = relayService;
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
                                 Model model) {
        try {
            String errorMessage = validateGuardConfiguration(relayNickname, relayPort, controlPort);
            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage);
                return "setup";
            }

            GuardRelayConfig config = createGuardConfig(relayNickname, relayPort, relayContact, controlPort, relayBandwidth);
            createTorrcFile(relayNickname, config);

            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }

        return "setup";
    }

    private String validateGuardConfiguration(String relayNickname, int relayPort, int controlPort) {
        if (!relayService.arePortsAvailable(relayNickname, relayPort, controlPort)) {
            return "One or more ports are already in use.";
        }

        if (RelayUtils.relayExists(relayNickname)) {
            return "A relay with the same nickname already exists.";
        }

        if (!RelayUtils.portsAreAvailable(relayNickname, relayPort, controlPort)) {
            return "One or more ports are already in use.";
        }

        return null;
    }

    private void createTorrcFile(String relayNickname, GuardRelayConfig config) {
        String torrcFileName = TORRC_FILE_PREFIX + relayNickname + "_guard";
        Path torrcFilePath = Paths.get(TORRC_DIRECTORY_PATH, torrcFileName).toAbsolutePath().normalize();

        if (!torrcFilePath.toFile().exists()) {
            TorrcFileCreator.createTorrcFile(torrcFilePath.toString(), config);
        }
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
    public ResponseEntity<Map<String, Object>> checkGuardLimit() {
        Map<String, Object> response = new HashMap<>();
        int guardCount = relayService.getGuardCount();

        if (!RelayService.isLimitOn()) {
            response.put("guardLimitReached", false);
            response.put("guardCount", guardCount);
            return ResponseEntity.ok(response);
        }

        response.put("guardLimitReached", guardCount >= 8);
        response.put("guardCount", guardCount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/limit-state-and-guard-count")
    public ResponseEntity<Map<String, Object>> getLimitStateAndGuardCount() {
        Map<String, Object> response = new HashMap<>();
        response.put("limitOn", RelayService.isLimitOn());
        response.put("guardCount", relayService.getGuardCount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/guard-configured")
    public ResponseEntity<Map<String, Boolean>> checkGuardConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        // Logic to check if a guard is configured
        boolean isGuardConfigured = relayService.getGuardCount() > 0;
        response.put("guardConfigured", isGuardConfigured);
        return ResponseEntity.ok(response);
    }
}
