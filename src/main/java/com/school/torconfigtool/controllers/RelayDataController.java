package com.school.torconfigtool.controllers;

import com.school.torconfigtool.models.RelayData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api")
public class RelayDataController {

    @PostMapping("/relay-data")
    public ResponseEntity<String> receiveRelayData(@RequestBody RelayData relayData) {
        // You can now use the relayData object directly in your application
        // For example, you can log the values or process them as needed

        System.out.println("Downloaded: " + relayData.getDownload() + " bytes/s");
        System.out.println("Uploaded: " + relayData.getUpload() + " bytes/s");

        return ResponseEntity.ok("Data received successfully");
    }
}
