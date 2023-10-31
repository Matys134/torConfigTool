package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.RelayData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RelayDataController {

    private static final int MAX_DATA_SIZE = 50;
    private Map<Integer, List<RelayData>> relayDataMap = new HashMap<>();

    @PostMapping("/relay-data/{relayId}")
    public ResponseEntity<String> receiveRelayData(@PathVariable int relayId, @RequestBody RelayData relayData) {
        // Get the relay data list for the given relayId
        List<RelayData> relayDataList = relayDataMap.computeIfAbsent(relayId, k -> new ArrayList<>());

        // Add the relay data
        relayDataList.add(relayData);

        // Check if the list size exceeds the maximum allowed size
        if (relayDataList.size() > MAX_DATA_SIZE) {
            // Remove the oldest data entries to limit the list size
            relayDataList.subList(0, relayDataList.size() - MAX_DATA_SIZE).clear();
        }

        return ResponseEntity.ok("Data received successfully for Relay ID: " + relayId);
    }

    @GetMapping("/relay-data/{relayId}")
    public List<RelayData> getRelayData(@PathVariable int relayId) {
        // Get the relay data list for the given relayId

        // Return the list of stored relay data
        return relayDataMap.getOrDefault(relayId, new ArrayList<>());
    }

    @GetMapping("/control-ports")
    public List<Integer> getControlPorts() {
        return new ArrayList<>(relayDataMap.keySet());
    }
}
