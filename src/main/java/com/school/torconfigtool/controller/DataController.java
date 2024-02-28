package com.school.torconfigtool.controller;

import com.school.torconfigtool.service.DataService;
import com.school.torconfigtool.model.RelayData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a REST controller that handles requests related to relay data and events.
 */
@RestController
@RequestMapping("/api")
public class DataController {

    // Maps to store relay data and events, with relay ID as the key
    private final Map<Integer, Deque<RelayData>> relayDataMap = new ConcurrentHashMap<>();
    private final Map<Integer, Deque<String>> relayEventMap = new ConcurrentHashMap<>();

    // Service to handle operations related to relay data and events
    private final DataService dataService;

    /**
     * Constructor for the DataController class.
     * @param dataService The service to handle operations related to relay data and events.
     */
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * Endpoint to receive relay data.
     * @param relayId The ID of the relay.
     * @param relayData The data of the relay.
     * @return A response entity with a success message.
     */
    @PostMapping("/data/{relayId}")
    public ResponseEntity<String> receiveRelayData(@PathVariable int relayId, @RequestBody RelayData relayData) {
        dataService.handleRelayData(relayId, relayData, relayDataMap);
        return ResponseEntity.ok("Data received successfully for Relay ID: " + relayId);
    }

    /**
     * Endpoint to receive relay event.
     * @param relayId The ID of the relay.
     * @param eventData The event data of the relay.
     * @return A response entity with a success message.
     */
    @PostMapping("/data/{relayId}/event")
    public ResponseEntity<String> receiveRelayEvent(@PathVariable int relayId, @RequestBody Map<String, String> eventData) {
        dataService.handleRelayEvent(relayId, eventData, relayDataMap, relayEventMap);
        return ResponseEntity.ok("Event received successfully for Relay ID: " + relayId);
    }

    /**
     * Endpoint to get relay events.
     * @param relayId The ID of the relay.
     * @return A list of events for the given relay ID.
     */
    @GetMapping("/data/{relayId}/events")
    public List<String> getRelayEvents(@PathVariable int relayId) {
        return new ArrayList<>(relayEventMap.getOrDefault(relayId, new LinkedList<>()));
    }

    /**
     * Endpoint to get relay data.
     * @param relayId The ID of the relay.
     * @return A list of data for the given relay ID.
     */
    @GetMapping("/data/{relayId}")
    public List<RelayData> getRelayData(@PathVariable int relayId) {
        synchronized (relayDataMap) {
            return new ArrayList<>(relayDataMap.getOrDefault(relayId, new LinkedList<>()));
        }
    }

    /**
     * Endpoint to get control ports.
     * @return A list of control ports.
     */
    @GetMapping("/control-ports")
    public List<Integer> getControlPorts() {
        return new ArrayList<>(relayDataMap.keySet());
    }
}