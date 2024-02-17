package com.school.torconfigtool.data;

import com.school.torconfigtool.relay.RelayData;
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
     * @param relayData The relay data to be added.
     * @return A response indicating the success of the operation.
     */
    @PostMapping("/data/{relayId}")
    public ResponseEntity<String> receiveRelayData(@PathVariable int relayId, @RequestBody RelayData relayData) {

        Deque<RelayData> relayDataQueue = relayDataMap.computeIfAbsent(relayId, k -> new LinkedList<>());
        dataService.addRelayData(relayDataQueue, relayData);

        return ResponseEntity.ok("Data received successfully for Relay ID: " + relayId);
    }

    /**
     * Endpoint to receive relay events.
     * @param relayId The ID of the relay.
     * @param eventData The event data to be added.
     * @return A response indicating the success of the operation.
     */
    @PostMapping("/data/{relayId}/event")
    public ResponseEntity<String> receiveRelayEvent(@PathVariable int relayId, @RequestBody Map<String, String> eventData) {
        String event = eventData.get("event");

        // Add the event to the relay data
        Deque<RelayData> relayDataQueue = relayDataMap.get(relayId);
        if (relayDataQueue != null) {
            RelayData relayData = new RelayData();
            relayData.setEvent(event);
            dataService.addRelayData(relayDataQueue, relayData);
        }

        // Add the event to the relay events
        Deque<String> relayEventQueue = relayEventMap.computeIfAbsent(relayId, k -> new LinkedList<>());
        dataService.addRelayEvent(relayEventQueue, event);

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