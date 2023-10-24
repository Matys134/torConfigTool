package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.RelayData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RelayDataController {

    private RelayData latestRelayData;

    @PostMapping("/relay-data")
    public ResponseEntity<String> receiveRelayData(@RequestBody RelayData relayData) {
        // Update the latest relay data
        latestRelayData = relayData;

        return ResponseEntity.ok("Data received successfully");
    }

    @GetMapping("/relay-data")
    public RelayData getLatestRelayData() {
        // Return the latest stored relay data
        return latestRelayData;
    }
}
