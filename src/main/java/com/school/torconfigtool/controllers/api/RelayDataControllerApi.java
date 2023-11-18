package com.school.torconfigtool.controllers.api;

import com.school.torconfigtool.models.RelayData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class RelayDataControllerApi {

    private static final int MAX_DATA_SIZE = 50;
    private final Map<Integer, Deque<RelayData>> relayDataMap = new ConcurrentHashMap<>();

    @PostMapping("/relay-data/{relayId}")
    public ResponseEntity<String> receiveRelayData(@PathVariable int relayId, @RequestBody RelayData relayData) {
        if (relayData == null || !isValidRelayData(relayData)) {
            return ResponseEntity.badRequest().body("Invalid data received for Relay ID: " + relayId);
        }

        Deque<RelayData> relayDataQueue = relayDataMap.computeIfAbsent(relayId, k -> new LinkedList<>());
        addRelayData(relayDataQueue, relayData);

        return ResponseEntity.ok("Data received successfully for Relay ID: " + relayId);
    }

    @GetMapping("/relay-data/{relayId}")
    public List<RelayData> getRelayData(@PathVariable int relayId) {
        return new ArrayList<>(relayDataMap.getOrDefault(relayId, new LinkedList<>()));
    }

    @GetMapping("/control-ports")
    public List<Integer> getControlPorts() {
        return new ArrayList<>(relayDataMap.keySet());
    }

    private void addRelayData(Deque<RelayData> relayDataQueue, RelayData relayData) {
        if (relayDataQueue.size() >= MAX_DATA_SIZE) {
            relayDataQueue.poll(); // Remove the oldest data entry
        }
        relayDataQueue.offer(relayData); // Add the new data entry
    }

    private boolean isValidRelayData(RelayData relayData) {
        return relayData.getBandwidth() > 0 && relayData.getUptime() > 0;
    }
}
