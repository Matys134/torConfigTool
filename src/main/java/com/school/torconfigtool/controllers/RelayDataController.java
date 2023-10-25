package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.RelayData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RelayDataController {

    private static final int MAX_DATA_SIZE = 50;
    private List<RelayData> relayDataList = new ArrayList<>();

    @PostMapping("/relay-data")
    public ResponseEntity<String> receiveRelayData(@RequestBody RelayData relayData) {
        // Add the relay data
        relayDataList.add(relayData);

        // Check if the list size exceeds the maximum allowed size
        if (relayDataList.size() > MAX_DATA_SIZE) {
            // Remove the oldest data entries to limit the list size
            relayDataList.subList(0, relayDataList.size() - MAX_DATA_SIZE).clear();
        }

        return ResponseEntity.ok("Data received successfully");
    }

    @GetMapping("/relay-data")
    public List<RelayData> getRelayData() {
        // Return the list of stored relay data
        return relayDataList;
    }
}
