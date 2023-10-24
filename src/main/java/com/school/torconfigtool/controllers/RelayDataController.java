package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.RelayData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RelayDataController {

    private List<RelayData> relayDataList = new ArrayList<>();

    @PostMapping("/relay-data")
    public ResponseEntity<String> receiveRelayData(@RequestBody RelayData relayData) {
        // Store the relay data
        relayDataList.add(relayData);

        return ResponseEntity.ok("Data received successfully");
    }

    @GetMapping("/relay-data")
    public List<RelayData> getRelayData() {
        // Return the list of stored relay data
        return relayDataList;
    }
}
