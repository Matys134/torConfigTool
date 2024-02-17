package com.school.torconfigtool;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/port")
public class PortController {

    @GetMapping("/check-availability")
    public ResponseEntity<?> checkPortAvailability(@RequestParam String nickname, @RequestParam int orPort, @RequestParam int controlPort) {
        boolean arePortsAvailable = RelayUtils.portsAreAvailable(nickname, orPort, controlPort);

        return ResponseEntity.ok(Map.of("available", arePortsAvailable));
    }
}