package com.school.torconfigtool;

import com.school.torconfigtool.model.GuardConfig;
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

/**
 * This controller class is responsible for handling requests related to Guard Relays.
 */
@Controller
@RequestMapping("/guard")
public class GuardController {

    private static final Logger logger = LoggerFactory.getLogger(GuardController.class);
    private static final String TORRC_DIRECTORY_PATH = "torrc/";
    private static final String TORRC_FILE_PREFIX = "torrc-";

    private final RelayService relayService;
    private final RelayOperationsController relayOperationController;
    private final GuardService guardService;


    public GuardController(RelayService relayService, RelayOperationsController relayOperationController, GuardService guardService) {
        this.relayService = relayService;
        this.relayOperationController = relayOperationController;
        this.guardService = guardService;
    }

    /**
     * Handles the request for the Guard Relay configuration form.
     *
     * @param model The model to be used for rendering the view.
     * @return The name of the view to be rendered.
     */
    @GetMapping
    public String guardConfigurationForm(Model model) {
        logger.info("Relay configuration form requested");
        RelayUtils.checkRunningRelays();

        List<String> availableRelayTypes = RelayUtils.determineAvailableRelayTypes();
        model.addAttribute("availableRelayTypes", availableRelayTypes);

        return "setup";
    }

    /**
     * Handles the request for configuring a Guard Relay.
     *
     * @param relayNickname       The nickname of the Guard Relay.
     * @param relayPort           The port of the Guard Relay.
     * @param relayContact        The contact information of the Guard Relay.
     * @param controlPort         The control port of the Guard Relay.
     * @param relayBandwidth      The bandwidth of the Guard Relay.
     * @param startRelayAfterConfig Whether to start the relay after configuration.
     * @param model               The model to be used for rendering the view.
     * @return The name of the view to be rendered.
     */
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

            GuardConfig config = guardService.createGuardConfig(relayNickname, relayPort, relayContact, controlPort, relayBandwidth);
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

    /**
     * Handles GET requests to the /limit-reached endpoint.
     * Checks if the guard limit has been reached.
     *
     * @return a response entity with the result
     */
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

    /**
     * Handles GET requests to the /bridge-configured endpoint.
     * Checks if a bridge has been configured.
     *
     * @return a response entity with the result
     */
    @GetMapping("/bridge-configured")
    public ResponseEntity<Map<String, Boolean>> checkBridgeConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("bridgeConfigured", relayService.getBridgeCount() > 0);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles GET requests to the /limit-state-and-guard-count endpoint.
     * Gets the limit state and guard count.
     *
     * @return a response entity with the result
     */
    @GetMapping("/limit-state-and-guard-count")
    public ResponseEntity<Map<String, Object>> getLimitStateAndGuardCount() {
        Map<String, Object> response = new HashMap<>();
        response.put("limitOn", RelayService.isLimitOn());
        response.put("guardCount", relayService.getGuardCount());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles GET requests to the /guard-configured endpoint.
     * Checks if a guard is configured.
     *
     * @return a response entity with the result
     */
    @GetMapping("/guard-configured")
    public ResponseEntity<Map<String, Boolean>> checkGuardConfigured() {
        Map<String, Boolean> response = new HashMap<>();
        // Logic to check if a guard is configured
        boolean isGuardConfigured = relayService.getGuardCount() > 0;
        response.put("guardConfigured", isGuardConfigured);
        return ResponseEntity.ok(response);
    }
}
