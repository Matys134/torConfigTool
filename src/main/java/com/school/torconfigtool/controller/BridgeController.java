package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * This is a Spring Boot controller for managing Tor bridges.
 * It provides endpoints for configuring, running, and managing Tor bridges.
 */
@Controller
@RequestMapping("/bridge")
public class BridgeController {

    private final RelayInformationService relayInformationService;
    private final NginxService nginxService;
    private final BridgeService bridgeService;
    private final SnowflakeProxyService snowflakeProxyService;

    /**
     * Constructor for the BridgeController.
     * @param relayInformationService The relay service.
     * @param nginxService The Nginx service.
     * @param bridgeService The bridge service.
     * @param snowflakeProxyService The snowflake proxy service.
     */
    @Autowired
    public BridgeController(RelayInformationService relayInformationService, NginxService nginxService, BridgeService bridgeService, SnowflakeProxyService snowflakeProxyService) {
        this.relayInformationService = relayInformationService;
        this.nginxService = nginxService;
        this.bridgeService = bridgeService;
        this.snowflakeProxyService = snowflakeProxyService;
    }

    /**
     * Endpoint for getting the bridge configuration form.
     * @return The name of the setup view.
     */
    @GetMapping
    public String bridgeConfigurationForm() {
        return "setup";
    }

    /**
     * Endpoint for configuring the bridge.
     * @param bridgeType The type of bridge.
     * @param bridgePort The port of the bridge.
     * @param bridgeTransportListenAddr The transport listen to address of the bridge.
     * @param bridgeContact The contact of the bridge.
     * @param bridgeNickname The nickname of the bridge.
     * @param webtunnelDomain The domain of the web tunnel.
     * @param bridgeControlPort The control port of the bridge.
     * @param webtunnelUrl The URL of the web tunnel.
     * @param webtunnelPort The port of the web tunnel.
     * @param bridgeBandwidth The bandwidth of the bridge.
     * @param model The model for the view.
     * @return The name of the setup view.
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
            bridgeService.configureBridge(bridgeType, bridgePort, bridgeTransportListenAddr, bridgeContact, bridgeNickname, webtunnelDomain, bridgeControlPort, webtunnelUrl, webtunnelPort, bridgeBandwidth);
            model.addAttribute("successMessage", "Tor Bridge configured successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to configure Tor Bridge: " + e.getMessage());
        }
        return "setup";
    }

    /**
     * Endpoint for running the Snowflake proxy.
     * @return ResponseEntity<String> - The response entity containing the result of the operation and the corresponding HTTP status.
     */
    @PostMapping("/run-snowflake-proxy")
    public ResponseEntity<String> runSnowflakeProxy() {
        try {
            snowflakeProxyService.setupSnowflakeProxy();
            return new ResponseEntity<>("Snowflake proxy started successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error starting snowflake proxy: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint for checking if the bridge limit has been reached.
     * @param bridgeType - The type of bridge to check the limit for.
     * @return ResponseEntity<Map<String, Object>> - The response entity containing the map with the bridge limit reached status and the bridge counts.
     */
    @GetMapping("/limit-reached")
    public ResponseEntity<Map<String, Object>> checkBridgeLimit(@RequestParam String bridgeType) {
        Map<String, Object> response = bridgeService.checkBridgeLimit(bridgeType);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for setting up the bridge configuration.
     * @param model The model for the view.
     * @return The name of the setup view.
     */
    /*@GetMapping("/setup")
    public String setup(Model model) {
        model.addAttribute("bridgeLimitReached", relayInformationService.getBridgeCount() >= 2);
        return "setup";
    }*/

    /**
     * Endpoint for getting the running bridge type.
     * @return ResponseEntity<Map<String, String>> - The response entity containing the map with the running bridge type.
     */
    @GetMapping("/running-type")
    public ResponseEntity<Map<String, String>> getRunningBridgeType() {
        Map<String, String> response = relayInformationService.getRunningBridgeType();
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for reverting the Nginx configuration to its default state.
     * @return ResponseEntity<String> - The response entity containing the result of the operation and the corresponding HTTP status.
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
     * Endpoint for toggling the limit on the number of bridges that can be configured.
     * @return ResponseEntity<Void> - The response entity indicating the result of the operation.
     */
    @PostMapping("/toggle-limit")
    public ResponseEntity<Void> toggleLimit() {
        RelayInformationService.toggleLimit();
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for getting the state of the bridge limit.
     * @return ResponseEntity<Boolean> - The response entity containing the state of the bridge limit.
     */
    @GetMapping("/limit-state")
    public ResponseEntity<Boolean> getLimitState() {
        return ResponseEntity.ok(RelayInformationService.isLimitOn());
    }

    @GetMapping("/bridge-configured")
    public ResponseEntity<Map<String, Boolean>> checkBridgeConfigured() {
        return ResponseEntity.ok(bridgeService.checkBridgeConfigured());
    }
}