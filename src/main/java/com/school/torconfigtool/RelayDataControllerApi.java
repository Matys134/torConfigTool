package com.school.torconfigtool;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class RelayDataControllerApi {

    private static final int MAX_DATA_SIZE = 50;
    private static final int MAX_EVENT_SIZE = 10;
    private final Map<Integer, Deque<RelayData>> relayDataMap = new ConcurrentHashMap<>();
    private final Map<Integer, Deque<String>> relayEventMap = new ConcurrentHashMap<>();

    @PostMapping("/relay-data/{relayId}")
    public ResponseEntity<String> receiveRelayData(@PathVariable int relayId, @RequestBody RelayData relayData) {

        Deque<RelayData> relayDataQueue = relayDataMap.computeIfAbsent(relayId, k -> new LinkedList<>());
        addRelayData(relayDataQueue, relayData);

        return ResponseEntity.ok("Data received successfully for Relay ID: " + relayId);
    }

    @PostMapping("/relay-data/{relayId}/event")
    public ResponseEntity<String> receiveRelayEvent(@PathVariable int relayId, @RequestBody Map<String, String> eventData) {
        String event = eventData.get("event");

        // Add the event to the relay data
        Deque<RelayData> relayDataQueue = relayDataMap.get(relayId);
        if (relayDataQueue != null) {
            RelayData relayData = new RelayData();
            relayData.setEvent(event);
            addRelayData(relayDataQueue, relayData);
        }

        // Add the event to the relay events
        Deque<String> relayEventQueue = relayEventMap.computeIfAbsent(relayId, k -> new LinkedList<>());
        addRelayEvent(relayEventQueue, event);

        return ResponseEntity.ok("Event received successfully for Relay ID: " + relayId);
    }

    @GetMapping("/relay-data/{relayId}/events")
    public List<String> getRelayEvents(@PathVariable int relayId) {
        return new ArrayList<>(relayEventMap.getOrDefault(relayId, new LinkedList<>()));
    }

    @GetMapping("/relay-data/{relayId}")
    public List<RelayData> getRelayData(@PathVariable int relayId) {
        synchronized (relayDataMap) {
            return new ArrayList<>(relayDataMap.getOrDefault(relayId, new LinkedList<>()));
        }
    }

    @GetMapping("/control-ports")
    public List<Integer> getControlPorts() {
        return new ArrayList<>(relayDataMap.keySet());
    }

    private void addRelayData(Deque<RelayData> relayDataQueue, RelayData relayData) {
        if (relayDataQueue.size() >= MAX_DATA_SIZE) {
            relayDataQueue.poll(); // Remove the oldest data entry
        }
        if (relayData != null) {
            relayDataQueue.offer(relayData); // Add the new data entry only if it's not null
        }
    }

    private void addRelayEvent(Deque<String> relayEventQueue, String event) {
        if (relayEventQueue.size() >= MAX_EVENT_SIZE) {
            relayEventQueue.poll(); // Remove the oldest event
        }
        relayEventQueue.offer(event); // Add the new event
    }
}