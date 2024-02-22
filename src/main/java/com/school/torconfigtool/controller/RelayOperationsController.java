package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.RelayOperationsService;
import com.school.torconfigtool.service.SnowflakeProxyService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controller for handling relay operations.
 */
@Controller
@RequestMapping("/relay-operations")
public class RelayOperationsController {

    private final RelayOperationsService relayOperationsService;
    private final SnowflakeProxyService snowflakeProxyService;

    /**
     * Constructor for RelayOperationsController.
     * @param relayOperationsService The service to handle relay operations.
     */
    public RelayOperationsController(RelayOperationsService relayOperationsService, SnowflakeProxyService snowflakeProxyService) {
        this.relayOperationsService = relayOperationsService;
        this.snowflakeProxyService = snowflakeProxyService;
        this.relayOperationsService.createDataDirectory();
    }

    /**
     * Handles GET requests to "/relay-operations".
     * @param model The model to be used.
     * @return The name of the view to be rendered.
     */
    @GetMapping
    public String relayOperations(Model model) {
        relayOperationsService.relayOperations(model);
        return "relay-operations";
    }

    /**
     * Handles POST requests to "/relay-operations/stop".
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
     * Handles POST requests to "/relay-operations/start".
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
     * Handles GET requests to "/relay-operations/status".
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
     * Handles POST requests to "/relay-operations/remove".
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
     * Handles POST requests to "/relay-operations/open-orport".
     * @param relayNickname The nickname of the relay.
     * @param relayType The type of the relay.
     * @return The result of the operation.
     */
    @PostMapping("/open-orport")
    @ResponseBody
    public Map<String, Object> openOrPort(@RequestParam String relayNickname, @RequestParam String relayType) {
        return relayOperationsService.openOrPort(relayNickname, relayType);
    }

    /**
     * Handles POST requests to "/relay-operations/toggle-upnp".
     * @param enable A boolean indicating whether to enable or disable UPnP.
     * @return The result of the operation.
     */
    @PostMapping("/toggle-upnp")
    @ResponseBody
    public Map<String, Object> toggleUPnP(@RequestParam boolean enable) {
        return relayOperationsService.toggleUPnP(enable);
    }

    @PostMapping("/start-snowflake-proxy")
    @ResponseBody
    public ResponseEntity<String> startSnowflakeProxy() {
        snowflakeProxyService.startSnowflakeProxy();
        return ResponseEntity.ok("Snowflake proxy started successfully");
    }

    @PostMapping("/stop-snowflake-proxy")
    @ResponseBody
    public ResponseEntity<String> stopSnowflakeProxy() {
        snowflakeProxyService.stopSnowflakeProxy();
        return ResponseEntity.ok("Snowflake proxy stopped successfully");
    }
}