package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.RelayOperationsService;
import com.school.torconfigtool.service.SnowflakeProxyService;
import com.school.torconfigtool.service.UPnPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * This is a REST controller that handles operations related to relay operations.
 * It provides endpoints for starting, stopping, getting the status of, and removing relays.
 * It also provides endpoints for managing the Snowflake proxy and checking the availability of UPnP.
 */
@RestController
@RequestMapping("/relay-operations-api")
public class RelayOperationsRestController {

    private final RelayOperationsService relayOperationsService;
    private final SnowflakeProxyService snowflakeProxyService;
    private final UPnPService upnpService;

    /**
     * Constructor for RelayOperationsApiController.
     * @param relayOperationsService The service for relay operations.
     * @param snowflakeProxyService The service for Snowflake proxy operations.
     * @param upnpService The service for UPnP operations.
     */
    @Autowired
    public RelayOperationsRestController(RelayOperationsService relayOperationsService, SnowflakeProxyService snowflakeProxyService, UPnPService upnpService) {
        this.relayOperationsService = relayOperationsService;
        this.snowflakeProxyService = snowflakeProxyService;
        this.upnpService = upnpService;
    }

    /**
     * Endpoint to stop a relay.
     * @param relayNickname The nickname of the relay.
     * @param relayType The type of the relay.
     * @param model The model to be used.
     * @return The result of the operation.
     */
    @PostMapping("/stop")
    public String stopRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        return relayOperationsService.stopRelay(relayNickname, relayType, model);
    }

    /**
     * Endpoint to start a relay.
     * @param relayNickname The nickname of the relay.
     * @param relayType The type of the relay.
     * @param model The model to be used.
     * @return The result of the operation.
     */
    @PostMapping("/start")
    public String startRelay(@RequestParam String relayNickname, @RequestParam String relayType, Model model) {
        return relayOperationsService.startRelay(relayNickname, relayType, model);
    }

    /**
     * Endpoint to get the status of a relay.
     * @param relayNickname The nickname of the relay.
     * @param relayType The type of the relay.
     * @return The status of the relay.
     */
    @GetMapping("/status")
    @ResponseBody
    public String getRelayStatus(@RequestParam String relayNickname, @RequestParam String relayType) {
        return relayOperationsService.getRelayStatus(relayNickname, relayType);
    }

    /**
     * Endpoint to remove a relay.
     * @param relayNickname The nickname of the relay.
     * @param relayType The type of the relay.
     * @return The result of the operation.
     */
    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeRelay(@RequestParam String relayNickname, @RequestParam String relayType) {
        return relayOperationsService.removeRelay(relayNickname, relayType);
    }

    /**
     * Endpoint to open the ORPort of a relay.
     * @param relayNickname The nickname of the relay.
     * @param relayType The type of the relay.
     * @return The result of the operation.
     */
    @PostMapping("/open-orport")
    @ResponseBody
    public Map<String, Object> openOrPort(@RequestParam String relayNickname, @RequestParam String relayType) {
        return upnpService.openPorts(relayNickname, relayType);
    }

    /**
     * Endpoint to toggle UPnP.
     * @param enable A boolean indicating whether to enable or disable UPnP.
     * @return The result of the operation.
     */
    @PostMapping("/toggle-upnp")
    @ResponseBody
    public Map<String, Object> toggleUPnP(@RequestParam boolean enable) {
        return upnpService.toggleUPnP(enable);
    }

    /**
     * Endpoint to start the Snowflake proxy.
     * @return The result of the operation.
     */
    @PostMapping("/start-snowflake-proxy")
    @ResponseBody
    public ResponseEntity<String> startSnowflakeProxy() {
        snowflakeProxyService.startSnowflakeProxy();
        return ResponseEntity.ok("Snowflake proxy started successfully");
    }

    /**
     * Endpoint to stop the Snowflake proxy.
     * @return The result of the operation.
     */
    @PostMapping("/stop-snowflake-proxy")
    @ResponseBody
    public ResponseEntity<String> stopSnowflakeProxy() {
        snowflakeProxyService.stopSnowflakeProxy();
        return ResponseEntity.ok("Snowflake proxy stopped successfully");
    }

    /**
     * Endpoint to check the availability of UPnP.
     * @return A boolean indicating whether UPnP is available.
     */
    @GetMapping("/upnp-availability")
    @ResponseBody
    public boolean checkUPnPAvailability() {
        return upnpService.isUPnPAvailable();
    }

    /**
     * Endpoint to remove the Snowflake proxy.
     * @return The result of the operation.
     */
    @PostMapping("/remove-snowflake-proxy")
    @ResponseBody
    public ResponseEntity<String> removeSnowflakeProxy() {
        snowflakeProxyService.removeSnowflakeProxy();
        return ResponseEntity.ok("Snowflake proxy removed successfully");
    }
}