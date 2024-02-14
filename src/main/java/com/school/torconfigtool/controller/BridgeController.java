package com.school.torconfigtool.controller;

import com.school.torconfigtool.BridgeRelayConfig;
import com.school.torconfigtool.BridgeSetupService;
import com.school.torconfigtool.NginxService;
import com.school.torconfigtool.RelayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a controller for managing bridge configurations.
 * It provides endpoints for configuring, running, and checking the status of bridges.
 */
@Controller
@RequestMapping("/bridge")
public class BridgeController {

    private final RelayService relayService;
    private final NginxService nginxService;
    private final BridgeSetupService bridgeSetupService;

    private static final Logger logger = LoggerFactory.getLogger(BridgeController.class);

    /**
     * Constructor for BridgeController.
     * @param relayService service for managing relay configurations
     * @param nginxService service for managing Nginx configurations
     * @param bridgeSetupService service for setting up bridges
     */
    @Autowired
    public BridgeController(RelayService relayService, NginxService nginxService, BridgeSetupService bridgeSetupService) {
        this.relayService = relayService;
        this.nginxService = nginxService;
        this.bridgeSetupService = bridgeSetupService;
    }

    /**
     * Endpoint for getting the bridge configuration form.
     * @return the name of the setup view
     */
    @GetMapping
    public String bridgeConfigurationForm() {
        return "setup";
    }

    /**
     * Endpoint for configuring a bridge.
     * @param bridgeType the type of the bridge
     * @param bridgePort the port of the bridge
     * @param bridgeTransportListenAddr the transport listen address of the bridge
     * @param bridgeContact the contact of the bridge
     * @param bridgeNickname the nickname of the bridge
     * @param webtunnelDomain the domain of the web tunnel
     * @param bridgeControlPort the control port of the bridge
     * @param webtunnelUrl the URL of the web tunnel
     * @param webtunnelPort the port of the web tunnel
     * @param bridgeBandwidth the bandwidth of the bridge
     * @param model the model to add attributes to
     * @return the name of the setup view
     */
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
                                  @RequestParam(required = false) Integer bridgeBandwidth,
                                  Model model) {
        try {
            bridgeSetupService.configureBridge(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain, bridgeControlPort, webtunnelUrl, webtunnelPort, bridgeBandwidth, model);
        } catch (Exception e) {
            logger.error("Error during Tor Relay configuration", e);
            model.addAttribute("errorMessage", "Failed to configure Tor Relay.");
        }
        return "setup";
    }

    /**
     * Endpoint for running a snowflake proxy.
     * @return a response entity with a message and a status
     */
    @PostMapping("/run-snowflake-proxy")
    public ResponseEntity<String> runSnowflakeProxy() {
        try {
            BridgeRelayConfig bridgeRelayConfig = new BridgeRelayConfig();
            bridgeRelayConfig.runSnowflakeProxy();
            return new ResponseEntity<>("Snowflake proxy started successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error starting snowflake proxy: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint for checking if the bridge limit has been reached.
     * @param bridgeType the type of the bridge
     * @return a response entity with a map containing the bridge limit status and the bridge count
     */
    @GetMapping("/limit-reached")
    public ResponseEntity<Map<String, Object>> checkBridgeLimit(@RequestParam String bridgeType) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Integer> bridgeCountByType = relayService.getBridgeCountByType();

        if (!RelayService.isLimitOn()) {
            response.put("bridgeLimitReached", false);
            response.put("bridgeCount", bridgeCountByType.get(bridgeType));
            return ResponseEntity.ok(response);
        }

        switch (bridgeType) {
            case "obfs4":
                response.put("bridgeLimitReached", bridgeCountByType.get("obfs4") >= 2);
                response.put("bridgeCount", bridgeCountByType.get("obfs4"));
                break;
            case "webtunnel":
                response.put("bridgeLimitReached", bridgeCountByType.get("webtunnel") >= 1);
                response.put("bridgeCount", bridgeCountByType.get("webtunnel"));
                break;
            case "snowflake":
                response.put("bridgeLimitReached", bridgeCountByType.get("snowflake") >= 1);
                response.put("bridgeCount", bridgeCountByType.get("snowflake"));
                break;
            default:
                response.put("bridgeLimitReached", false);
                response.put("bridgeCount", 0);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for getting the setup view.
     * @param model the model to add attributes to
     * @return the name of the setup view
     */
    @GetMapping("/setup")
    public String setup(Model model) {
        model.addAttribute("bridgeLimitReached", relayService.getBridgeCount() >= 2);
        return "setup";
    }

    /**
     * Endpoint for getting the running bridge type.
     * @return a response entity with a map containing the running bridge type
     */
    @GetMapping("/running-type")
    public ResponseEntity<Map<String, String>> getRunningBridgeType() {
        Map<String, String> response = relayService.getRunningBridgeType();
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for reverting the Nginx configuration.
     * @return a response entity with a message and a status
     */
    @PostMapping("/revert-nginx-config")
    public ResponseEntity<String> revertNginxConfig() {
        try {
            nginxService.revertNginxDefaultConfig();
            return new ResponseEntity<>("Nginx configuration reverted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error reverting Nginx configuration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint for toggling the limit.
     * @return a response entity with a status
     */
    @PostMapping("/toggle-limit")
    public ResponseEntity<Void> toggleLimit() {
        RelayService.toggleLimit();
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for getting the limit state.
     * @return a response entity with the limit state
     */
    @GetMapping("/limit-state")
    public ResponseEntity<Boolean> getLimitState() {
        return ResponseEntity.ok(RelayService.isLimitOn());
    }
}