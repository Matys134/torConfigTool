package com.school.torconfigtool.relay.controller;

import com.school.torconfigtool.relay.service.RelayDataService;
import com.school.torconfigtool.relay.data.RelayData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * This class is a REST controller for handling requests related to relay data.
 * It uses the RelayDataService for the business logic.
 */
@RestController
@RequestMapping("/api")
public class RelayDataControllerApi {

    private final RelayDataService relayDataService;

    /**
     * Constructor for the RelayDataControllerApi class.
     * @param relayDataService The service to be used for handling relay data.
     */
    @Autowired
    public RelayDataControllerApi(RelayDataService relayDataService) {
        this.relayDataService = relayDataService;
    }

    /**
     * Endpoint for receiving relay data.
     * @param relayId The ID of the relay.
     * @param relayData The relay data to be added.
     * @return A response indicating the success of the operation.
     */
    @PostMapping("/relay-data/{relayId}")
    public ResponseEntity<String> receiveRelayData(@PathVariable int relayId, @RequestBody RelayData relayData) {
        relayDataService.addRelayData(relayId, relayData);
        return ResponseEntity.ok("Data received successfully for Relay ID: " + relayId);
    }

    /**
     * Endpoint for receiving relay events.
     * @param relayId The ID of the relay.
     * @param eventData The event data to be added.
     * @return A response indicating the success of the operation.
     */
    @PostMapping("/relay-data/{relayId}/event")
    public ResponseEntity<String> receiveRelayEvent(@PathVariable int relayId, @RequestBody Map<String, String> eventData) {
        String event = eventData.get("event");
        relayDataService.addRelayEvent(relayId, event);
        return ResponseEntity.ok("Event received successfully for Relay ID: " + relayId);
    }

    /**
     * Endpoint for getting relay events.
     * @param relayId The ID of the relay.
     * @return A list of relay events.
     */
    @GetMapping("/relay-data/{relayId}/events")
    public List<String> getRelayEvents(@PathVariable int relayId) {
        return relayDataService.getRelayEvents(relayId);
    }

    /**
     * Endpoint for getting relay data.
     * @param relayId The ID of the relay.
     * @return A list of relay data.
     */
    @GetMapping("/relay-data/{relayId}")
    public List<RelayData> getRelayData(@PathVariable int relayId) {
        return relayDataService.getRelayData(relayId);
    }

    /**
     * Endpoint for getting control ports.
     * @return A list of control ports.
     */
    @GetMapping("/control-ports")
    public List<Integer> getControlPorts() {
        return relayDataService.getControlPorts();
    }
}