package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.BridgeService;
import com.school.torconfigtool.service.RelayInformationService;
import com.school.torconfigtool.service.SnowflakeProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * This is a REST controller for the Bridge API.
 * It provides endpoints for managing and interacting with the bridge.
 */
@RestController
@RequestMapping("/bridge-api")
public class BridgeRestController {

    private final RelayInformationService relayInformationService;
    private final BridgeService bridgeService;
    private final SnowflakeProxyService snowflakeProxyService;

    /**
     * Constructor for the BridgeApiController.
     * It initializes the RelayInformationService, BridgeService, and SnowflakeProxyService.
     *
     * @param relayInformationService - service for managing relay information
     * @param bridgeService - service for managing bridges
     * @param snowflakeProxyService - service for managing the Snowflake proxy
     */
    @Autowired
    public BridgeRestController(RelayInformationService relayInformationService, BridgeService bridgeService, SnowflakeProxyService snowflakeProxyService) {
        this.relayInformationService = relayInformationService;
        this.bridgeService = bridgeService;
        this.snowflakeProxyService = snowflakeProxyService;
    }

    /**
     * Endpoint for running the Snowflake proxy.
     * It starts the Snowflake proxy and returns a response entity with the result of the operation.
     *
     * @return ResponseEntity<String> - The response entity containing the result of the operation and the corresponding HTTP status.
     */
    @PostMapping("/setup-snowflake-proxy")
    public ResponseEntity<String> setupSnowflakeProxy() {
        try {
            snowflakeProxyService.setupSnowflakeProxy();
            return new ResponseEntity<>("Snowflake proxy ready", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error configuring snowflake proxy: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint for checking if the bridge limit has been reached.
     * It checks the limit for the specified bridge type and returns a response entity with the result.
     *
     * @param bridgeType - The type of bridge to check the limit for.
     * @return ResponseEntity<Map<String, Object>> - The response entity containing the map with the bridge limit reached status and the bridge counts.
     */
    @GetMapping("/bridges/bridge-count")
    public ResponseEntity<Map<String, Object>> checkBridgeLimit(@RequestParam String bridgeType) {
        Map<String, Object> response = bridgeService.countBridges(bridgeType);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for getting the running bridge type.
     * It retrieves the running bridge type and returns a response entity with the result.
     *
     * @return ResponseEntity<Map<String, String>> - The response entity containing the map with the running bridge type.
     */
    @GetMapping("/bridges/configured-type")
    public ResponseEntity<Map<String, String>> getConfiguredBridgeType() {
        Map<String, String> response = relayInformationService.getConfiguredBridgeType();
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for checking if the bridge is configured.
     * It checks if the bridge is configured and returns a response entity with the result.
     *
     * @return ResponseEntity<Map<String, Boolean>> - The response entity containing the map with the bridge configured status.
     */
    @GetMapping("/bridge-configured")
    public ResponseEntity<Map<String, Boolean>> isBridgeLimitReached() {
        return ResponseEntity.ok(bridgeService.checkBridgeConfigured());
    }
}