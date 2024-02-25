package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.GuardService;
import com.school.torconfigtool.util.RelayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * This controller class is responsible for handling requests related to Guard Relays.
 */
@Controller
@RequestMapping("/guard")
public class GuardController {

    // Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(GuardController.class);

    // Service instance for Guard operations
    private final GuardService guardService;

    /**
     * Constructor for GuardController
     *
     * @param guardService The service to be used for Guard operations.
     */
    public GuardController(GuardService guardService) {
        this.guardService = guardService;
    }

    /**
     * Handles the request for the Guard Relay configuration form.
     *
     * @return The name of the view to be rendered.
     */
    @GetMapping
    public String guardConfigurationForm() {
        logger.info("Relay configuration form requested");
        RelayUtils.checkRunningRelays();

        return "setup";
    }

    /**
     * Handles the POST request to configure a Guard Relay.
     *
     * @param relayNickname The nickname of the relay.
     * @param relayPort The port of the relay.
     * @param relayContact The contact of the relay.
     * @param controlPort The control port of the relay.
     * @param guardBandwidth The bandwidth of the relay (optional).
     * @param model The model to be used for rendering the view.
     * @return The name of the view to be rendered.
     */
    @PostMapping("/configure")
    public String configureGuard(@RequestParam String relayNickname,
                                 @RequestParam int relayPort,
                                 @RequestParam String relayContact,
                                 @RequestParam int controlPort,
                                 @RequestParam(required = false) Integer guardBandwidth,
                                 Model model) {
        try {
            guardService.configureGuard(relayNickname, relayPort, relayContact, controlPort, guardBandwidth);
            model.addAttribute("successMessage", "Tor Relay configured successfully!");
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay: " + e.getMessage());
        }
        return "setup";
    }

    /**
     * Handles the GET request to check if the Guard limit has been reached.
     *
     * @return A ResponseEntity containing a map with the result.
     */
    @GetMapping("/limit-reached")
    public ResponseEntity<Map<String, Object>> checkGuardLimit() {
        return ResponseEntity.ok(guardService.checkGuardLimit());
    }

    /**
     * Handles the GET request to check if a Bridge has been configured.
     *
     * @return A ResponseEntity containing a map with the result.
     */
    @GetMapping("/bridge-configured")
    public ResponseEntity<Map<String, Boolean>> checkBridgeConfigured() {
        return ResponseEntity.ok(guardService.checkBridgeConfigured());
    }

    /**
     * Handles the GET request to get the limit state and Guard count.
     *
     * @return A ResponseEntity containing a map with the result.
     */
    @GetMapping("/limit-state-and-guard-count")
    public ResponseEntity<Map<String, Object>> getLimitStateAndGuardCount() {
        return ResponseEntity.ok(guardService.getLimitStateAndGuardCount());
    }

    /**
     * Handles the GET request to check if a Guard has been configured.
     *
     * @return A ResponseEntity containing a map with the result.
     */
    @GetMapping("/guard-configured")
    public ResponseEntity<Map<String, Boolean>> checkGuardConfigured() {
        return ResponseEntity.ok(guardService.checkGuardConfigured());
    }
}